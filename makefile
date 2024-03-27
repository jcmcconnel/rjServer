# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1
#


SOURCEPATH=src
CLASSPATH=build
COMPILER=javac -sourcepath $(SOURCEPATH) -classpath $(CLASSPATH) -d $(CLASSPATH)


.PHONY: all
all:
	$(COMPILER) -Xlint @source_files 

.PHONY: clean
clean:
	rm -r build

.PHONY: copy-pages
copy-pages:
	cp -r src/pages build

.PHONY: test-server
test-server:
	cp -r src/pages build && cd build && java djava.main ./pages 5000

.PHONY: test-client
test-client:
	cd build && java djava.Client 5000

.PHONY: test-raw-client
test-raw-client:
	cd build && java djava.Client --http-test 5000

.PHONY: test-help
test-help:
	cd build && java djava.main

