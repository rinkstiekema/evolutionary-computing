echo BentCigarFunction >>BentCigar.txt
FOR /l %%A IN (1,1,20) DO (
    echo %%A>>BentCigar.txt
    FOR /l %%B IN (1,1,20) DO (
        java -jar testrun.jar -submission=player02Tournament -evaluation=BentCigarFunction -seed=%%A >>BentCigar.txt
    )
)

echo SchaffersEvaluation >>Schaffers.txt
FOR /l %%A IN (1,1,20) DO (
    SET seed=%%A
    echo %seed% >>Schaffers.txt
    FOR /l %%B IN (1,1,20) DO (
        java -jar testrun.jar -submission=player02Tournament -evaluation=SchaffersEvaluation -seed=%%A >>Schaffers.txt
    )
)