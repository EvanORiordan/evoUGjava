# Evolutionary UG/DG project

Files required to recreate results of "Fairness in the Dictator Game with Edge Weight Learning" paper:
- src/DGAlgorithmPaper1.java
- src/Player.java

How to run: 
- In DGAlgorithmPaper1.main(), assign values to fundamental parameters: runs, ROC, rows, gens, EPR.
- Assign value to experiment_series. 
- If experiment_series set to true, assign values to varying_parameter, num_experiments and variation to indicate which parameter will be varied across the experiment series, by how much per experiment and how many experiment will take place respectively.

Example experiment settings: 
- runs=1000, 
- rows=20, 
- gens=15000, 
- EPR=10, 
- ROC=0.02, 
- experiment_series=false.

Example experiment series settings: 
- runs=1000, 
- rows=10, 
- gens=10000, 
- EPR=5, 
- ROC=0.01, 
- experiment_series=true, 
- varying_parameter=ROC, 
- num_experiments=5, 
- variation=0.005.

If recreating the results obtained from DGAlgorithmPaper1.java, please cite the following paper: insertLinkToPaperHere.
