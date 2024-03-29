# "Unless the Lord builds the house, they labor in vain who build it" Psalm 127:1
#


SOURCEPATH=src
CLASSPATH=build
COMPILER=javac -sourcepath $(SOURCEPATH) -classpath $(CLASSPATH) -d $(CLASSPATH)


.PHONY: all
all:
	$(COMPILER) @source_files 

.PHONY: clean
clean:
	rm -r build

.PHONY: copy-pages
copy-pages:
	cp -r src/responder/pages build/responder

.PHONY: test-server
test-server:
	cp -r src/responder/pages build/responder && cd build && java server.main -i --conf responder/pages/default.conf

.PHONY: test-client
test-client:
	cd build && java Client 5000

.PHONY: test-raw-client
test-raw-client:
	cd build && java Client --http-test 5000

