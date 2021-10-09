.PHONY: build

build: executable
	./gradlew build

clean:
	rm -rf build


executable:
	chmod +x ./gradlew