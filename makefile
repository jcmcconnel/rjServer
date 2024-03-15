# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1
#


SOURCEPATH=src
COMPILER=javac -sourcepath $(SOURCEPATH) -classpath build -d build

all: 
	$(COMPILER) @source_files 
	#cp src/*.* build

.PHONY: clean
clean:
	rm -r build

.PHONY: test
test:
	cd build && java djava.main 5000

.PHONY: test-client
test-client:
	cd build && java djava.Client

.PHONY: test-help
test-help:
	cd build && java djava.main
