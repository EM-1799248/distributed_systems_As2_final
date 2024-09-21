# List of all Java files required for the application
SOURCES = src/AggregationServer.java src/GETClient.java src/ContentServer.java src/LamportClock.java

# Default port number
PORT ?= 4567

# Default target: compile everything automatically
all: bin compile

# Ensure bin directory is created before compiling
bin:
	mkdir -p bin

# Compile the Java files required for the application, as defined in the SOURCES variable
compile:
	javac -cp .idea/libraries/gson-2.11.0.jar -d bin $(SOURCES)

# Make command to run the Aggregation Server
run-server:
	@echo "Running the Aggregation Server..."
	#cd bin && java AggregationServer $(PORT)
	 cd bin && java -verbose AggregationServer $(PORT)

# Make command to run the GET Client
run-client:
	@echo "Running the GET Client..."
	cd bin && java GETClient http://localhost:$(PORT) /stationID

# Make command to run the Content Server
run-content:
	@echo "Running the Content Server..."
	cd bin && java ContentServer http://localhost:$(PORT) data.txt

# Clean up compiled classes
clean:
	rm -rf bin/*.class
	rm -rf bin
