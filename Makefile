# Makefile - Proyecto 3 LdP
# Uso: make para compilar y make run para ejecutar

JC = javac
JAVA = java
SOURCES = Main.java Estacionamiento.java Vehiculo.java UnidadCarga.java
MAIN = Main
CONFIG = config.txt

all: compile

compile:
	$(JC) $(SOURCES)

run: compile
	$(JAVA) $(MAIN) $(CONFIG)

clean:
	rm -f *.class

.PHONY: all compile run clean