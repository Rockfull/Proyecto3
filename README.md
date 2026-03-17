# Proyecto 3: The Electric Rush Hour

## Autores
* **Oriana Rocafull** - C.I: 25386529

## Decision de diseño que tomé yo:

- **El monitor (Estacionamiento)**: usé `synchronized` en todos los métodos que tocan la matriz para que no choquen dos carros nunca.

- **Batería y cargadores**: cuando un carro se queda en 0 entra en `wait()` dentro de `esperarRecarga()`. Los cargadores están en un `while` esperando `notifyAll()`. Cuando cargan, notifican de vuelta.

- **Evitar deadlocks**: puse movimiento con dirección random (`1` o `-1`) y si no puede moverse hace `sleep` con tiempo random. Eso rompió los ciclos y la simulación empezó a avanzar.

- **Consumo de batería**: solo resto 1 cuando el movimiento fue exitoso. Si lo restaba en cada intento, se quedaban sin pila en 5 segundos y no había quien saliera.

Al final cambié varias cosas después de probar mucho:
- Subí la recarga de 10 a 12 porque con 10 el carro 0 casi nunca llegaba.
- Agregué `volatile` en los flags porque sin eso a veces los hilos no veían que la simulación había terminado.
- En los cargadores puse un sleep pequeño para no matar la CPU.

## Formato del archivo config.txt
Cada linea representa un vehiculo: `[ID] [Orientación] [Fila] [Columna] [Longitud] [Batería]`
La última linea indica la cantidad de `Unidades de Carga`.
## Instrucciones de Uso

- **Compilación**
make          # compila

- **Ejecucion**
make run      # ejecuta con config.txt