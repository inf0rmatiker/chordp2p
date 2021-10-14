.PHONY: build

build: executable
	./gradlew build

clean:
	rm -rf build

executable:
	chmod +x ./gradlew

discovery:
	java -cp build/libs/chordp2p-uber.jar org.chord.Main --discovery-node

# Example: make store-data DISCOVERY=shark
store-data:
	java -cp build/libs/chordp2p-uber.jar org.chord.Main --store-data $(DISCOVERY)

# Example: make peer DISCOVERY=shark ID=aaaa
peer:
	java -cp build/libs/chordp2p-uber.jar org.chord.Main --peer $(DISCOVERY) $(ID)