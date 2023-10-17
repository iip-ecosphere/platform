/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.hm23;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.AppIntercom;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import iip.datatypes.AiResult;
import iip.datatypes.BeckhoffInput;
import iip.datatypes.BeckhoffInputImpl;
import iip.datatypes.BeckhoffOutput;
import iip.datatypes.Command;
import iip.datatypes.DecisionResult;
import iip.datatypes.DecisionResultImpl;
import iip.datatypes.MdzhOutput;
import iip.datatypes.PlcOutput;
import iip.impl.ActionDeciderImpl;

/**
 * Implements the action decider.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ActionDecider extends ActionDeciderImpl {

    public static final long OVERALL_TIMEOUT = TimeUnit.MINUTES.toMillis(2);
    public static final String ERROR_CLASS_SHATTER = "Shatter";
    public static final String ERROR_CLASS_SCRATCH = "Scratch";
    public static final String ERROR_CLASS_GEOMETRY = "Geometry";
    public static final String ERROR_CLASS_CAR_MISSING = "Car missing";
    public static final String ERROR_CLASS_NORMAL = "Normal";

    /**
     * Agreed scan positions with Beckhoff PLC.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected enum ScanPos {
        RIGHT(1),  // light on
        TOP(2),    // light on
        LEFT(3),   // light on
        BASE(4);   // light off
        
        private short sceneNr;
        
        /**
         * Creates a scene position constant.
         * 
         * @param sceneNr the scene number as on Beckhoff PLC
         */
        private ScanPos(int sceneNr) {
            this.sceneNr = (short) sceneNr;
        }
        
        /**
         * Returns the scene number for Beckhoff PLC.
         * 
         * @return the scene number
         */
        public short getSceneNr() {
            return sceneNr;
        }
        
    }

    // getParameterRobotIP() now available after constructor and reconfigure was executed
    
    private static final boolean BYPASS_BECKHOFF = Boolean.valueOf(
        System.getProperty("iip.app.hm23.imgBypassBeckhoff", "false"));
    private Map<String, MdzhOutput> productData = new HashMap<>();
    private List<AiResult> aiResults = new ArrayList<AiResult>();
    private File storageFolder;
    private long aiResultsTimestamp;
    private long lastAiResultsTimestamp;
    private ScanPos nextPos = ScanPos.LEFT;

    private State currentState = State.STOP;
    private State nextWaitState = State.RUNNING;
    private long lastStateChange;
    private short lastPic = -1;
    @SuppressWarnings("unused")
    private long lastModelChange = -1;
    private long lastAiResult = -1;
    
    private AppIntercom<Command> intercom = new AppIntercom<Command>(d -> processCommand(d), Command.class);
    private CobotCommands cobotCommands = new CobotCommands(() -> requestImageScene(nextPos, false), 
        pi -> ingestPlcInput(pi));
    private Set<String> inputAi = null;

    /**
     * Internal states for longer running operations.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected enum State {
        STOP,
        RUNNING, // sub-divided by robot movements, picture taking and result receiving
        WAIT_FOR_ROBOT_BUSY,
        WAIT_FOR_ROBOT_DONE,
        DRIVE_ACTIVE
    }
    
    /**
     * Fallback constructor, also used for testing main program.
     */
    public ActionDecider() {
        super(ServiceKind.TRANSFORMATION_SERVICE);
        setCurrentState(State.STOP);
        initInputAi();
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public ActionDecider(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
        initInputAi();
    }

    /**
     * Initializes the input AI id set.
     */
    private void initInputAi() {
        String tmp = getParameterInputAiId();
        if (null == tmp) {
            inputAi = null;
        } else {
            String[] ids = tmp.split(",");
            inputAi = new HashSet<String>();
            for (String id : ids) {
                inputAi.add(id.trim());
            }
        }
    }

    /**
     * Returns whether AI input shall be considered.
     * 
     * @param input the AI input
     * @return {@code true} for consider, {@code false} else
     */
    private boolean considerInput(AiResult input) {
        return null == inputAi || (null != input.getAiId() && inputAi.contains(input.getAiId()));
    }
    
    /**
     * Returns the current processing state.
     * 
     * @return the state
     */
    protected State getCurrentState() {
        return currentState;
    }
    
    /**
     * Returns the number of actually stored AI results.
     * 
     * @return the number
     */
    protected int getAiResultsCount() {
        return aiResults.size();
    }
    
    /**
     * Returns the storage base folder, i.e., the folder where the {@link #getStorageFolder()} shall 
     * be located in. [mocking]
     *  
     * @return the storage base folder
     */
    protected File getStorageBaseFolder() {
        return new File(System.getProperty("user.home", "."));
    }
    
    /**
     * Returns the storage folder for images, results, etc.
     * 
     * @return the storage folder
     */
    private File getStorageFolder() {
        if (null == storageFolder) {
            storageFolder = new File(getStorageBaseFolder(), "iipHm23Data");
            storageFolder.mkdirs();
        }
        return storageFolder;
    }

    /**
     * Returns a valid timestamp for persistence, either {@link #aiResultsTimestamp} or the actual system time.
     * 
     * @param fallback if the process is already gone, not positive for none
     * @return the timestamp
     */
    private long getAiTimestamp(long fallback) {
        return aiResultsTimestamp > 0 ? aiResultsTimestamp 
            : (fallback > 0 ? fallback : System.currentTimeMillis());
    }

    /**
     * Returns an AI file id.
     * 
     * @param timestamp the timestamp from {@link #getAiTimestamp(long)}
     * @return the file id, potentially including the image sequence nr
     */
    private String getAiFileId(long timestamp) {
        return aiResultsTimestamp > 0 ? timestamp + "-" + aiResults.size() : String.valueOf(timestamp);
    }

    /**
     * Repressents a persisted result, just for writing.
     * 
     * @author Holger Eichelberger, SSE
     */
    class PersistedResult {

        private String ai;
        private long timestamp;
        private int imageNr;

        /**
         * Creates an instance based on the given timestamp.
         * 
         * @param timestamp the timestamp {@link ActionDecider#getAiTimestamp(long)}
         */
        private PersistedResult(long timestamp) {
            this.timestamp = timestamp;
            imageNr = aiResultsTimestamp > 0 ? aiResults.size() : -1;
        }
        
        /**
         * Returns the AI service name. [jackson]
         * 
         * @return the AI service name
         */
        public String getAi() {
            return ai;
        }
        
        /**
         * Returns the timestamp of the first image. [jackson]
         * 
         * @return the timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Returns the image number. [jackson]
         * 
         * @return the image number, negative if N/A
         */
        public int getImageNr() {
            return imageNr;
        }

    }

    /**
     * Repressents a persisted AI result, just for writing.
     * 
     * @author Holger Eichelberger, SSE
     */
    class PersistedAiResult extends PersistedResult {

        private AiResult result;
        
        /**
         * Creates an instance based on the given timestamp and result.
         * 
         * @param timestamp the timestamp {@link ActionDecider#getAiTimestamp(long)}
         * @param result the AI result to store
         */
        private PersistedAiResult(long timestamp, AiResult result) {
            super(timestamp);
            this.result = result;
        }

        /**
         * Returns the AI result. [jackson]
         * 
         * @return the AI result
         */
        public AiResult getResult() {
            return result;
        }
        
    }

    /**
     * Stores the AI result to disk.
     * 
     * @param data the data to store
     */
    protected void store(AiResult data) {
        File folder = getStorageFolder();
        long timestamp = getAiTimestamp(0);
        String id = getAiFileId(timestamp);
        
        File f = new File(folder, id + ".res");
        PersistedAiResult res = new PersistedAiResult(timestamp, data);
        storeToJson(f, res);
    }
    
    /**
     * Stores the given data to JSON.
     * 
     * @param file the file to store the data to
     * @param data the data to store
     */
    protected void storeToJson(File file, Object data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            System.out.println("Cannot write " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }
    
    /**
     * Repressents a persisted AI feedback, just for writing.
     * 
     * @author Holger Eichelberger, SSE
     */
    class PersistedAiFeedback extends PersistedResult {
        
        private String feedback;

        /**
         * Creates an instance based on the given timestamp and feedback.
         * 
         * @param timestamp the timestamp {@link ActionDecider#getAiTimestamp(long)}
         * @param feedback the feedback
         */
        private PersistedAiFeedback(long timestamp, String feedback) {
            super(timestamp);
            this.feedback = feedback;
        }
        
        /**
         * Returns the feedback. [jackson]
         * 
         * @return the feedback
         */
        public String getFeedback() {
            return feedback;
        }

    }
    
    /**
     * Stores the feedback on AI.
     * 
     * @param feedback the feedback
     */
    protected void storeFeedback(String feedback) {
        File folder = getStorageFolder();
        long timestamp = getAiTimestamp(lastAiResultsTimestamp);
        String id = getAiFileId(timestamp);
        
        File f = new File(folder, id + ".fbk");
        PersistedAiFeedback res = new PersistedAiFeedback(timestamp, feedback);
        storeToJson(f, res);
    }


    @Override
    public void processBeckhoffOutput(BeckhoffOutput data) {
        if (currentState == State.RUNNING && data.getIPicCounter() != lastPic) {
            // just pass on from opc to image source
            lastPic = data.getIPicCounter();
            requestImage(data.getIPicCounter(), nextPos, true);
        }
    }

    @Override
    public void processAiResult(AiResult data) {
        long now = System.currentTimeMillis();
        if (State.RUNNING == currentState && considerInput(data) && (lastAiResult < 0 || now - lastAiResult > 500)) {
            System.out.println("Processing AI result");
            lastAiResult = now;
            if (aiResults.size() == 0) {
                aiResultsTimestamp = now;
            }
            store(data);
            aiResults.add(data);
            if (aiResults.size() == 1) {
                robotDoCarScanInPos(ScanPos.TOP);
            } else if (aiResults.size() == 2) {
                robotDoCarScanInPos(ScanPos.RIGHT);
            } else if (aiResults.size() >= 3) { // actually three, but who knows
                robotDoCarScanInPos(ScanPos.BASE);
                DecisionResult res = aggregateResults(aiResults, getParameterErrorThreshold());
                res.setImageUri(aiResults.stream().map(a->a.getImageUri()).toArray(String[]::new));
                res.setRobotId(getParameterRobotId());
                ingestDecisionResult(res);
                
                /*if (!res.getIo()) {
                    sendRobotCommand(PLC_ROBOT_NEXT_NO_CAR);
                    setCurrentState(State.WAIT_FOR_ROBOT_BUSY);
                    if (TimeUtils.waitFor(() -> currentState == State.WAIT_FOR_ROBOT_BUSY, 
                        TIMEDIFF_ROBOT_BUSY_SAFE_INT, 200)) {
                        setCurrentState(State.WAIT_FOR_ROBOT_DONE);
                    }
                }*/
                setCurrentState(State.STOP); // we are done here
            }
        }
    }

    /**
     * Aggregate the AI results. [public for testing]
     * 
     * @param aiResults the collected AI results for a certain method
     * @param threshold the error/confidence threshold
     * @return the aggregated output of the action decider without {@link DecisionResult#getIo()}.
     */
    public static DecisionResult aggregateResults(List<AiResult> aiResults, double threshold) {
        DecisionResult res = null;
        if (aiResults.size() >= 3) { // actually only three, but who knows
            res = new DecisionResultImpl();
            Map<String, Double> errors = new HashMap<>();
            double[] normalConf = new double[3];
            String[] imageUri = new String[3];
            
            // calculate maximal of all ("Normal" will be overridden), select normals, set AI id/Model id, 
            // pruned errors for individual picture error display/feedback in UI
            for (int i = 0; i < 3; i++) {
                AiResult ai = aiResults.get(i);
                String[] ers = ai.getError();
                double[] conf = ai.getErrorConfidence();
                for (int e = 0; e < Math.min(ers.length, conf.length); e++) {
                    String errCls = ers[e];
                    if (errors.containsKey(errCls)) {
                        double combVal;
                        if (errCls.equals(ERROR_CLASS_NORMAL)) {
                            combVal = Math.min(errors.get(errCls), conf[e]);
                        } else {
                            combVal = Math.max(errors.get(errCls), conf[e]);
                        }
                        errors.put(ers[e], combVal); // maximize
                    } else {
                        errors.put(ers[e], conf[e]); // initialize
                    }
                    if (errCls.equals(ERROR_CLASS_NORMAL)) {
                        normalConf[i] = conf[e];
                    }
                }
                res.setAiId(ai.getAiId()); // just take over, shall be the same for all pics
                res.setModelId(ai.getModelId()); // just take over, shall be the same for all pics
                res.setRobotId(ai.getRobotId()); // just take over, shall be the same for all pics
                String[] tmp = pruneErrors(ai.getError(), ai.getErrorConfidence(), threshold);
                switch (i) {
                case 0: // implicit left
                    res.setImg1Error(tmp);
                    break;
                case 1: // implicit top
                    res.setImg2Error(tmp);
                    break;
                case 2: // implicit right
                    res.setImg3Error(tmp);
                    break;
                default:
                    break;
                }
                imageUri[i] = ai.getImageUri();
            }
            double normalizedConfDiff = normalizeAiResult(normalConf,  maxAnomalyConf(errors));
            errors.put(ERROR_CLASS_NORMAL, normalizedConfDiff);
            boolean carNormal = normalizedConfDiff >= threshold;
            res.setIoReason(carNormal ? 2 : 1);
            
            boolean carMissing = errors.get(ERROR_CLASS_CAR_MISSING) < 0.5;
            errors.put(ERROR_CLASS_CAR_MISSING, (1 - errors.get(ERROR_CLASS_CAR_MISSING)));
            //Overwrite the previous decision only if car is missing!
            //Only if the car was IO AND it is not missing, keep it true, false otherwise
            res.setIo(carNormal && !carMissing);
            //The error reason should be fit for the biggest error
            //IF there is no error but the car is missing set it to the missing car value
            if (carMissing) {
                res.setIoReason(0);
            }
            
            // maximum values/recalculated normal value -> output error in usual format
            String[] ers = new String[errors.size()];
            double[] conf = new double[errors.size()];
            int i = 0;
            for (Map.Entry<String, Double> e: errors.entrySet()) {
                ers[i] = e.getKey();
                conf[i++] = e.getValue();
            }
            res.setError(ers);
            res.setErrorConfidence(conf);
            res.setImageUri(imageUri);
        }
        return res;
    }
    
    /**
     * Normalize and prepare a uniform normal value for the result given.
     * @param normalConf the different values for the normal confidence.
     * @param maxAnomalyConf the highest error confidence over all images and types.
     * @return the aggregated normal value.
     */
    public static double normalizeAiResult(double[] normalConf, double maxAnomalyConf) {
        // Sofiane's normal calculation and thresholding
        double sideNormalConf = Math.pow(normalConf[0] * normalConf[2], 0.5);
        double minNormalConf = Math.min(sideNormalConf, normalConf[1]);
        //double highestAnomalyConf = maxAnomalyConf(errors);
        // may become negative
        double confDifference = minNormalConf - maxAnomalyConf;
        //if normal values are bigger than error, this will be > 0.5 else smaller
        double normalizedConfDiff = (confDifference + 1) / 2; 
        return normalizedConfDiff;
    }

    /**
     * Gets a single confidence and returns 0 if there is none.
     * 
     * @param errorsConf the confidences
     * @param errorCls the error class name
     * @return the confidence
     */
    private static double getConf(Map<String, Double> errorsConf, String errorCls) {
        Double val = errorsConf.get(errorCls);
        return null == val ? 0.0 : val;
    }

    /**
     * Maximizes the confidence of the (maximized) anomaly class confidences.
     * 
     * @param errorsConf the confidences
     * @return the maximum confidence
     */
    private static double maxAnomalyConf(Map<String, Double> errorsConf) {
        return Math.max(
            Math.max(getConf(errorsConf, ERROR_CLASS_SHATTER), getConf(errorsConf, ERROR_CLASS_SCRATCH)), 
            getConf(errorsConf, ERROR_CLASS_GEOMETRY));
    }
    
    /**
     * Prunes {@code ers}/{@code conf} based on thresholding for {@code threshold}.
     * 
     * @param ers the error classes for one result
     * @param conf the confidences for one result
     * @param threshold the overall threshold
     * @return the error classes over the threshold
     */
    private static String[] pruneErrors(String[] ers, double[] conf, double threshold) {
        List<String> result = new ArrayList<String>();
        for (int e = 0; e < Math.min(ers.length, conf.length); e++) {
            if (conf[e] > threshold) {
                result.add(ers[e]);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the PC robot busy operating from {@code data} dependent on {@link #getParameterRobotId()}.
     * 
     * @param data the PLC output data instance
     * @return PC busy operating
     */
    private boolean getHW_StartProcess(PlcOutput data) {
        return getParameterRobotId() == 1 ? data.getHW_Btn1() : data.getHW_Btn2();
    }
    
    @Override
    public void processPlcOutput(PlcOutput data) {
        cobotCommands.setLastPlcOutput(data);
        if (!data.getSafetyOk()) {
            System.out.println("Safety stop!");
            Transport.sendTraceRecord(new TraceRecord(getId(), TraceRecord.ACTION_SENDING, 
                Commands.createCommand(Commands.NOTIFY_COBOT_SAFETY, String.valueOf(getParameterRobotId()))));
            setCurrentState(State.STOP);
        } else if (getHW_StartProcess(data)) {
            handleCommand(Commands.REQUEST_START_QUALITY_DETECTION, String.valueOf(getParameterRobotId()));
        } //else if (data.getHW_Btn0()) {
          //  cobotCommands.sendCommand(getParameterRobotId(), CobotCommands.PLC_ROBOT_NEXT_QUIT);
          //  setCurrentState(State.STOP);
            /*if (State.STOP == currentState // don't change AI while running
                && (lastModelChange < 0 || System.currentTimeMillis() - lastModelChange > 2000)) { 
                sendCommand(Commands.SEND_MODEL_CHANGE_TO_AI, "");
                lastModelChange = System.currentTimeMillis();
            }*/
        //}
        if (State.WAIT_FOR_ROBOT_BUSY == currentState) {
            if (data.getUR_BusyOperating()) { 
                setCurrentState(State.WAIT_FOR_ROBOT_DONE);
            }            
        } else if (State.WAIT_FOR_ROBOT_DONE == currentState) {
            if (!data.getUR_BusyOperating()) { 
                if (timeSinceLastStateChange() > CobotCommands.TIMEDIFF_ROBOT_BUSY_SAFE) {
                    System.out.println("Waiting for cobot timed out. STOP.");
                    setCurrentState(State.STOP);
                } else {
                    setCurrentState(nextWaitState); // usually WAITING -> RUNNING, different for drive
                    if (nextPos != ScanPos.BASE) {
                        requestImage((short) 0, nextPos, false);
                    }
                }
            }
        }
    }

    /**
     * Time since last state change.
     * 
     * @return time in ms
     */
    private long timeSinceLastStateChange() {
        return System.currentTimeMillis() - lastStateChange;
    }

    @Override
    public void processMdzhOutput(MdzhOutput data) {
        productData.put(data.getProductId(), data);
    }

    @Override
    public void processCommand(Command data) {
        handleCommand(Commands.valueOfSafe(data), data.getStringParam());
    }
    
    /**
     * Requests an image.
     * 
     * @param imageId the image id (returned from Beckhoff)
     * @param side the expected side to take the image
     * @param toCam send the request directly to cam or to Beckhoff if not {@code iip.app.hm23.imgBypassBeckhoff} is set
     */
    private void requestImage(short imageId, ScanPos side, boolean toCam) {
        if (!BYPASS_BECKHOFF) {
            TimeUtils.sleep(1500);
        }
        if (toCam || BYPASS_BECKHOFF) {
            sendCommand(Commands.SOURCE_TAKE_PICTURE, imageId + ";" + side.name().toLowerCase());
        } else {
            requestImageScene(side, true);
        }
    }

    /**
     * Requests an an image scene, optionally also a picture.
     * 
     * @param side the expected side to take the image
     * @param triggerImage if an image shall be taken/triggered
     */
    private void requestImageScene(ScanPos side, boolean triggerImage) {
        BeckhoffInput input = new BeckhoffInputImpl();
        input.setBPicTrigger(triggerImage);
        input.setIPicScene(side.getSceneNr()); // Beckhoff PLC starts with 1
        ingestBeckhoffInput(input);
    }
    
    /**
     * Tries to parse an int from {@code param}.
     * 
     * @param param the parameter as string
     * @param dflt the default value if param is <b>null</b> or does not represent an int
     * @return the int value or {@code dflt}
     */
    private int getInt(String param, int dflt) {
        int result = dflt;
        if (null != param) {
            try {
                result = Integer.parseInt(param);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return result;
    }
    
    /**
     * Returns the cobot commands instance.
     * 
     * @return the cobot commands instance
     */
    protected CobotCommands getCobotCommands() {
        return cobotCommands;
    }
    
    /**
     * Handles a command, represents the state machine transitions.
     * 
     * @param cmd the command
     * @param param the parameter for the command, may be <b>null</b> or empty for none
     */
    protected void handleCommand(Commands cmd, String param) {
        System.out.println("Action Decider: Received command " + cmd + " Current state: " + currentState);
        switch (cmd) {
        case SEND_FEEDBACK_TO_AI: // can be done all the time
            storeFeedback(param);
            break;
        case REQUEST_START_QUALITY_DETECTION:
            boolean readyForRequest = cobotCommands.readyForRequest();
            if (currentState == State.STOP && getInt(param, -1) == getParameterRobotId() && readyForRequest) {
                setCurrentState(State.RUNNING);
                robotDoCarScanInPos(ScanPos.LEFT);
            } else {
                System.out.println("Action Decider: Cannot start process. Current state: " 
                    + currentState + ", robot id: " + getInt(param, -1) + " " + getParameterRobotId() 
                    + " readyForRequest: " + readyForRequest + ". Ignored.");
            }
            break;
        case REQUEST_DRIVE:
            if (currentState == State.STOP && cobotCommands.isInSafePosition()) {
                if (CobotCommands.sendIntercom(intercom, Commands.createCommand(Commands.RESPONSE_DRIVE_ACK, null))) {
                    setCurrentState(State.DRIVE_ACTIVE);
                } // we are already in STOP
            } else {
                System.out.println("Cannot allocate drive as either app state is not STOP (" + currentState 
                    + ") or cobot is not in safe position.");
                CobotCommands.sendIntercom(intercom, Commands.createCommand(Commands.RESPONSE_DRIVE_NACK, null));
                // we are already in STOP
            }
            break;
        case DRIVE_DONE:
        case DRIVE_INTERRUPTED:
            if (currentState == State.DRIVE_ACTIVE) {
                setCurrentState(State.STOP);
                if (Commands.DRIVE_DONE == cmd) {
                    handleCommand(Commands.REQUEST_START_QUALITY_DETECTION, String.valueOf(getParameterRobotId()));
                }
            }
            break;
        case REQUEST_QUIT:
            if (currentState == State.RUNNING || currentState == State.WAIT_FOR_ROBOT_BUSY 
                || currentState  == State.WAIT_FOR_ROBOT_DONE) {
                cobotCommands.sendCommand(getParameterRobotId(), CobotCommands.PLC_ROBOT_NEXT_QUIT);
                setCurrentState(State.STOP);
            } else {
                System.out.println("Action Decider: Cannot send quit to robot. Process is not running. Ignored.");
            }
            break;
        case DRIVE_QUERY_CAR_AAS:
            System.out.println("Querying car AAS for " + param);
            sendCommand(Commands.QUERY_CAR_AAS, param); // just pass on
            break;
        case SOURCE_TAKE_PICTURE: // not here
        case INVALID:
        default:
            break;
        }
        if (System.currentTimeMillis() - lastStateChange > OVERALL_TIMEOUT) {
            System.out.println("DECIDER TIMEOUT: Reset to Stop");
            setCurrentState(State.STOP);
        }
    }
        
    /**
     * Requests the robot to do the car scan in given position. [public for testing]
     * 
     * @param pos the position to drive the robot to (1 or 2 [else])
     */
    public void robotDoCarScanInPos(ScanPos pos) {
        int next = -1;
        boolean oneShot = false;
        switch (pos) {
        case LEFT:
            next = CobotCommands.PLC_ROBOT_DEFAULT_START; 
            break;
        case TOP:
            next = CobotCommands.PLC_ROBOT_NEXT_ANY; // we have a picture, just do the next
            break;
        case RIGHT:
            next = CobotCommands.PLC_ROBOT_NEXT_ANY; // we have a picture, just do the next
            break;
        case BASE:
            next = CobotCommands.PLC_ROBOT_NEXT_BASE;
            oneShot = true;
            break;
        default:
            break;
        }
        if (next >= 0) {
            nextPos = pos;
            cobotCommands.sendCommand(getParameterRobotId(), next);
            if (!oneShot) {
                setCurrentState(State.WAIT_FOR_ROBOT_BUSY);
            }
        }
    }
    
    /**
     * Returns whether the PLC is ready to request. [mocking]
     * 
     * @return {@code true} if the PLC is ready, {@code false} else
     */
    /*protected boolean readyForRequestMock() {
        return cobotCommands.readyForRequest();
    }*/
    
    /**
     * Sends a robot command.
     * 
     * @param next the next command in pre-defined movement chain, ignored if negative
     */
    /*protected void sendRobotCommand(int next) {
        if (readyForRequest()) {
            PlcInput pi = new PlcInputImpl();
            if (next == CobotCommands.PLC_ROBOT_NEXT_QUIT) {
                pi.setPC_Quit(true);
            } else {
                requestImageScene(nextPos, false); // prepare cam, switch light on/off
                pi.setPC_Command01((short) next);
                if (next < 100) {
                    pi.setPC_StartOperation(true);
                }
            }
            ingestPlcInput(pi);
        } else {
            System.out.println("Cannot send robot command, PLC not ready");
        }
    }*/

    /**
     * Sends out a command.
     * 
     * @param cmd the command to be sent
     */
    protected void sendCommand(Command cmd) {
        if (!hasCommandIngestor()) {
            LoggerFactory.getLogger(Commands.class).error("Cannot send command. No ingestor present.");
        }
        ingestCommand(cmd);
    }
    
    /**
     * Sends out an internal application command.
     * 
     * @param cmd the command to be sent
     * @param param the parameter value, may be <b>null</b>
     */
    private void sendCommand(Commands cmd, String param) {
        if (!hasCommandIngestor()) {
            LoggerFactory.getLogger(Commands.class).error("Cannot send command. No ingestor present.");
        }
        ingestCommand(Commands.createCommand(cmd, param));
    }

    /**
     * Changes the state and records the change timestamp.
     * 
     * @param state the new state
     */
    private void setCurrentState(State state) {
        this.currentState = state;
        if (State.STOP == state) {
            aiResults.clear();
            lastAiResultsTimestamp = aiResultsTimestamp;
            aiResultsTimestamp = 0;
        }
        this.lastStateChange = System.currentTimeMillis();
    }

    @Override
    protected ServiceState start() throws ExecutionException {
        ServiceState result = super.start();
        intercom.start();
        return result;
    }

    @Override
    protected ServiceState stop() {
        intercom.stop();
        return super.stop(); 
    }
    
}
