import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Estacionamiento {
    private final int TAMANO = 6;
    private final int[][] celdas; // -1 = vacío, de lo contrario ID del vehículo
    private volatile boolean simulacionTerminada = false;
    private final Map<Integer, Vehiculo> vehiculos; // Mapa para el metodo estacionamiento
    private final Queue<Integer> colaRecarga; // Cola para vehículos que necesitan recarga

    public Estacionamiento() {
        celdas = new int[TAMANO][TAMANO];
        colaRecarga = new LinkedList<>(); // inicializamos la cola
        for (int i = 0; i < TAMANO; i++) {
            for (int j = 0; j < TAMANO; j++) {
                celdas[i][j] = -1;
            }
        }
        vehiculos = new HashMap<>(); // inicializamos el mapa
    }

    // Registrar un vehículo en sus posiciones iniciales
    public synchronized void colocarVehiculoInicial(Vehiculo v) {
        vehiculos.put(v.getId(), v); // Para almacenar los vehiculos
        int fila = v.getFila();
        int col = v.getColumna();
        int lon = v.getLongitud();
        char orient = v.getOrientacion();

        for (int i = 0; i < lon; i++) {
            int f = fila + (orient == 'V' ? i : 0);
            int c = col + (orient == 'H' ? i : 0);
            if (f < TAMANO && c < TAMANO) {
                celdas[f][c] = v.getId();
            } else {
                System.err.println("Posición fuera de rango para vehículo " + v.getId());
            }
        }
    }

    private void liberarCeldas(int fila, int columna, char orientacion, int longitud, int id) {
        for (int i = 0; i < longitud; i++) {
            int f = fila + (orientacion == 'V' ? i : 0);
            int c = columna + (orientacion == 'H' ? i : 0);
            celdas[f][c] = -1;
        }
    }

    private void ocuparCeldas(int fila, int columna, char orientacion, int longitud, int id) {
        for (int i = 0; i < longitud; i++) {
            int f = fila + (orientacion == 'V' ? i : 0);
            int c = columna + (orientacion == 'H' ? i : 0);
            celdas[f][c] = id;
        }
    }

    public synchronized boolean moverVehiculo(Vehiculo v, int deltaFila, int deltaColumna) throws InterruptedException {
        int filaActual = v.getFila();
        int colActual = v.getColumna();
        char orient = v.getOrientacion();
        int lon = v.getLongitud();

        // Calcular nueva posición (esquina superior izquierda)
        int nuevaFila = filaActual + deltaFila;
        int nuevaCol = colActual + deltaColumna;

        // Verificar límites (que no se salga del tablero)
        int filaFinal = nuevaFila + (orient == 'V' ? lon - 1 : 0);
        int colFinal = nuevaCol + (orient == 'H' ? lon - 1 : 0);
        if (filaFinal >= TAMANO || colFinal >= TAMANO) {
            return false; // No se puede mover fuera
        }

        // Esperar hasta que todas las celdas destino estén libres
        while (true) {
            boolean libre = true;
            for (int i = 0; i < lon; i++) {
                int f = nuevaFila + (orient == 'V' ? i : 0);
                int c = nuevaCol + (orient == 'H' ? i : 0);
                int idActual = celdas[f][c];
                // Permitir si está libre o es ocupada por el mismo vehículo
                if (idActual != -1 && idActual != v.getId()) {
                    libre = false;
                    break;
                }
            }
            if (libre)
                break;
            wait();
            if (simulacionTerminada)
                return false;
        }

        // Mover: liberar celdas viejas y ocupar nuevas
        liberarCeldas(filaActual, colActual, orient, lon, v.getId());
        ocuparCeldas(nuevaFila, nuevaCol, orient, lon, v.getId());
        v.setPosicion(nuevaFila, nuevaCol);

        notifyAll(); // Notificar a otros hilos que esperan
        return true;
    }

    // metodo esperar recarga
    public synchronized void esperarRecarga(Vehiculo v) throws InterruptedException {
        /*
         * Marcos: Modifique esta parte para tener una lista de vehiculos que necesitan
         * recarga y de ese modo se evita estar verificando a todos los vehiculos y se
         * recargan en orden
         */
        colaRecarga.add(v.getId()); // Agregar vehículo a la cola de recarga

        while (v.getBateria() == 0 && !simulacionTerminada) {
            wait();
        }

        colaRecarga.remove(v.getId()); // Eliminar vehículo de la cola al despertar
    }

    // Para que los vehículos notifiquen a los cargadores cuando se quedan sin
    // batería
    public synchronized void avisarVehiculoSinBateria() {
        notifyAll(); // Despierta a los cargadores que estén esperando
    }

    // public synchronized boolean recargarVehiculo(Vehiculo v) {
    // Esperar mientras la simulación no termine y la celda destino esté ocupada
    // Calcular nueva posición, verificar límites, etc.
    // Usar wait() si la celda destino está ocupada
    // Al final, liberar celdas viejas y ocupar nuevas, y notificar
    // }

    public synchronized void recargarEnergia() throws InterruptedException {
        while (!simulacionTerminada) {
            if (!colaRecarga.isEmpty()) {
                int idVehiculo = colaRecarga.peek(); // Obtener el ID del vehículo al frente de la cola
                Vehiculo v = vehiculos.get(idVehiculo); // Obtener el vehículo del mapa
                if (v != null && v.getBateria() == 0) {
                    v.setBateria(10);
                    System.out.println("Cargador recargó vehículo " + v.getId());
                    notifyAll(); // Despierta a los vehiculos que esperaban recarga
                    return;
                }
            }
            // No hay vehiculos sin batería, esperar
            while (colaRecarga.isEmpty() && !simulacionTerminada) {
                /*
                 * Marcos: Le agregue esta condición a recarga, ya que al hacer notifyAll, se
                 * despiertan
                 * todos y si no hay vehiculos que recargar se queda dormido
                 */
                wait();
            }
        }
    }

    public synchronized boolean simulacionTerminada() {
        return simulacionTerminada;
    }

    public synchronized void terminarSimulacion() {
        simulacionTerminada = true;
        notifyAll(); // despertar hilos que puedan estar esperando
    }

    // Otros métodos
}
