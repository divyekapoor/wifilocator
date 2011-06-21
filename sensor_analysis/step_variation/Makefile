all: *.plt cstep_files zcstep_files zcstep_clamped_files links
	gnuplot *.plt

cstep_files:drLog*.steps.csv
	@echo Building... $@
	@echo $?
	$(foreach stepFile, $?,	cat $(stepFile) | tr ',' '\t' | awk -f proposed_counter.awk > `basename $(stepFile) .steps.csv`.cstep.csv;)
	touch cstep_files

zcstep_files:drLog*.accel.csv
	@echo Building... $@
	@echo $?
	$(foreach accelFile, $?, cat $(accelFile) | tr ',' '\t' | awk -f zero_crossing.awk > `basename $(accelFile) .accel.csv`.zcstep.csv;)
	touch zcstep_files

zcstep_clamped_files:drLog*.accel.csv
	@echo Building... $@
	@echo $?
	$(foreach accelFile, $?, cat $(accelFile) | tr ',' '\t' | awk -f zero_crossing_clamped.awk > `basename $(accelFile) .accel.csv`.zcstep.clamped.csv;)
	touch zcstep_clamped_files

links:
	ls drLog*.accel.csv | sort -r | head -n1 | xargs -I XX ln -fs XX latest.accel.csv
	ls drLog*.steps.csv | sort -r | head -n1 | xargs -I XX ln -fs XX latest.steps.csv
	ls drLog*.cstep.csv | sort -r | head -n1 | xargs -I XX ln -fs XX latest.cstep.csv
	ls drLog*.zcstep.csv | sort -r | head -n1 | xargs -I XX ln -fs XX latest.zcstep.csv
	ls drLog*.zcstep.clamped.csv | sort -r | head -n1 | xargs -I XX ln -fs XX latest.zcstep.clamped.csv

clean:
	rm -f *.zcstep.csv *.zcstep.clamped.csv *.cstep.csv *.png
	rm -f zcstep_files zcstep_clamped_files cstep_files
	rm -f latest.*.csv

.PHONY: all clean
