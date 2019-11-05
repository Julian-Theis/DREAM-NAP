# Decay Replay Mining to Predict Next Process Events
This project is the implementation of the *Decay Replay Mining - Next Transition Prediction* (DREAM-NAP) approach described in the paper [Decay Replay Mining to Predict Next Process Events](https://ieeexplore.ieee.org/document/8811455) by [Julian Theis](http://julian-theis.github.io) and [Houshang Darabi](https://scholar.google.com/citations?user=PVzYdvoAAAAJ). Both authors are part of the [Process Mining and Intelligent System Analytics Team (PROMINENT)](https://prominent.uic.edu/) at the University of Illinois at Chicago, USA. 

## Approach
The DREAM-NAP approach consists of two stages: Decay Function Enhancement of a process model and Deep Learning using timed state samples.

![DREAM-NAP Flow Diagram](https://github.com/Julian-Theis/DREAM-NAP/blob/master/images/flow_diagram.gif?raw=true)

The folder *DREAM* contains the Java source code from process discovery to extration of timed state samples whereas *NAP* contains the Python files to train and evaluate the neural network.

## Results
### Benchmark Comparison
![Results](https://github.com/Julian-Theis/DREAM-NAP/blob/master/images/results.gif?raw=true)
Bold values designate that the proposed model outperforms state-of-the-art results.  
∗ denotes datasets that do not contain resources, therefore DREAM-NAPr is not applicable.  
∗∗ denotes that the source code of Breuker et al. [12] was not able to produce results on this dataset.

### Arithmetic Means of Ranks
![Arithmetic Ranks](https://github.com/Julian-Theis/DREAM-NAP/blob/master/images/ranks.gif?raw=true)

## ProM Plugin
We have implemented the Decay Replay Mining (DREAM) preprocessing approach as a [ProM](http://www.promtools.org) plugin. The plugin considers a Petri net process model as PNML and an CSV formatted event log as input and produces timed state samples that can be used for further machine learning and data science tasks. The plugin enhances and parametrizes each place of the process model with a time decay function. Afterwards, the event log is replayed on the enhanced model and timed state samples are extracted at every discrete timestep observed in the log.  
The plugin and its documentation is available here: [https://prominentlab.github.io/ProM-DREAM/](https://prominentlab.github.io/ProM-DREAM/).

## Citation
```
@article{theis2019decay,
  title={Decay Replay Mining to Predict Next Process Events},
  author={Theis, Julian and Darabi, Houshang},
  journal={IEEE Access},
  volume={7},
  pages={119787--119803},
  year={2019},
  publisher={IEEE}
}
```

## Remarks
We thank Raffaele Conforti for making his extensive research code available. This project requires the installation of his [Research Code](https://github.com/raffaeleconforti/ResearchCode).

[![HitCount](http://hits.dwyl.io/Julian-Theis/DREAM-NAP.svg)](http://hits.dwyl.io/Julian-Theis/DREAM-NAP)
