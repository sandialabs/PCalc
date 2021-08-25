#!/bin/bash

# Users may want to add the following line to their .bash_profile file
# PATH=$PATH:<path to directory where this file resides>

# the location of the pcalc jar file on the users system.
pcalc_jar=/Users/$USER/github/PCalcDevl/target/pcalc-3.2.2-jar-with-dependencies.jar

# oracle's ojbdc jar file, which is required to access an oracle database.  
# This file is not delivered with pcalc.  Users must locate this file on their sytem and
# specify its location here.
ojdbc_jar=/Users/$USER/jarfiles/ojdbc8.jar

# If rstt/slbm is to be used, specify the directory where the file libslbmjni.jnilib resides.
# If any one of [ RSTT_ROOT, RSTT_HOME, SLBM_ROOT, SLBM_HOME ] is specified in your .bash_profile
# then use of rstt/slbm will be possible without specifying slbm_libdir here.  
slbm_libdir=/Users/$USER/Documents/rstt/RSTT_v3.2.0/lib



if [ -z $ojdbc_jar ]; then 
	classpath=$pcalc_jar
else 
	classpath=$pcalc_jar:$ojdbc_jar
fi

# call java to run pcalc. Include java.library.path only if slbm_libdir is specified.
if [ -z $slbm_libdir ]; then 
	java -cp $classpath gov.sandia.gmp.pcalc.PCalc $@
else 
	java -Djava.library.path=$slbm_libdir -cp $classpath gov.sandia.gmp.pcalc.PCalc $@
fi
