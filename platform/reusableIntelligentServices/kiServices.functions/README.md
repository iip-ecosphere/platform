# IIP-Ecosphere platform: AI-Services (basic functions)

Basic reusable data processing and AI functions, not necessarily AI services.

Currently contains functions to load, encode and decode images.
	ImageEncodingDecoding contains methods to turn a base64 encoded byte string back into an BufferedImage, 
	as well as reading an image file from the file system and passing it on as base64 encoded byte string. 
	Further method to read in a base64 encoded byte string from a file.
	
	ImageProcessing is concerned with preprocessing steps for images i.e. cropping before further working on them.
	It contains methods to grayscale images and to turn them into black and white based on a threshold.
	Also it enables the rescaling of an image to make it smaller which can be beneficial for qr detection.
	
	QRCodeService contains the methods to read a qr code from an image or an base64 encoded byte string(which represents an image)
	If the readQR of java fails exists a backup python solution, the fallback solution will execute a separate pythonscript and
	try again to recognize the qr code again.
	
The python fallback solution has the requirements:

pyzbar
opencv-python
numpy
Pillow