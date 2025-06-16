# Evolutionary UG/DG project

Files required to recreate results of "Fairness in the Dictator Game with Edge Weight Learning" paper:
- src/DGAlgorithmPaper1.java
- src/Player.java

To assign values to the environmental parameters, edit code in setParams().

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

If recreating the results obtained from DGAlgorithmPaper1.java, please cite the associated paper: "On the Emergence of Fairness in the Evolutionary Dictator Game with Edge Weight Learning" by Evan O'Riordan, Frank Glavin and Colm O'Riordan.
