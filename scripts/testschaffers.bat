FOR /l %%A IN (1,1,20) DO (
    java -jar testrun.jar -submission=player2 -evaluation=SchaffersEvaluation -seed=%%A >>Schaffers.txt
)