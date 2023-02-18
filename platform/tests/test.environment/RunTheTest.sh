
jupyter nbconvert --to python 'TestManagementScript.ipynb'

Repetition=$(cat $1 | grep Repetition | rev | cut -d ' ' -f -1)

echo $Repetition

mkdir -p AllLogs

for i in `seq 1 $Repetition`;
do
  rm  Logs/*
  python3 TestManagementScript.py $1

  RunTime=$(date +'-%F-%T' | sed 's/:/-/g')
  echo $RunTime
  cp -R Logs AllLogs/Logs$RunTime
done
