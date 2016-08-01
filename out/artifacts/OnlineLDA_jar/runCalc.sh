for ((k=60; k<=1000; k+=20)); do
	for ((b=100; b<=100; b+=100)); do
		for t in 0.9; do
			for kappa in 0.9; do
				for a in 0.15; do
					for e in 15; do
						echo ${k}, ${b}, ${t}, ${kappa}, ${a}, ${e}
						time java -jar OnlineLDA.jar D:700000 K:${k} batchSize:${b} tau:${t} kappa:${kappa} alpha:${a} eta:${e} dataFile:/Users/johan.risch/Documents/Exjobb/jsStream/streamDumps/2015-06-16_1100.txt dict:/Users/johan.risch/Documents/Exjobb/jsStream/words/2015-06-16_1100.txt blocking:true >> /Users/johan.risch/Documents/Exjobb/jsStream/perplexity/D700000k${k}b${b}t${t}kappa${kappa}a${a}e${e}.txt
					done	
				done
			done
		done
	done
done



