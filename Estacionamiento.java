import java.util.HashMap;
import java.util.Map;

public class Estacionamiento {
    private final int TAMANO = 6;
    private final int[][] celdas; // -1 = vacio, de lo contrario ID del vehiculo
    private volatile boolean simulacionTerminada = false;
    private final Map<Integer, Vehiculo> vehiculos; // Mapa para el metodo estacionamiento

    public Estacionamiento() {
        celdas = new int[TAMANO][TAMANO];
        for (int i = 0; i < TAMANO; i++) {
            for (int j = 0; j < TAMANO; j++) {
                celdas[i][j] = -1;
            }
        }
        vehiculos = new HashMap<>(); // inicializamos el mapa
    }

    // Métodos auxiliares privados
    private void actualizarCeldas(Vehiculo v, int valor) {
        for (int i = 0; i < v.getLongitud(); i++) {
            int f = v.getFila() + (v.getOrientacion() == 'V' ? i : 0);
            int c = v.getColumna() + (v.getOrientacion() == 'H' ? i : 0);
            celdas[f][c] = valor;
        }
    }

    public synchronized void colocarVehiculoInicial(Vehiculo v) {
        vehiculos.put(v.getId(), v); // Lo guarda en el mapa para los cargadores
        actualizarCeldas(v, v.getId()); // Lo dibuja en la matriz inicial
    }

    public synchronized boolean moverVehiculo(Vehiculo v, int deltaFila, int deltaColumna) throws InterruptedException {
        if (simulacionTerminada)
            return false;
        int nuevaFila = v.getFila() + deltaFila;
        int nuevaCol = v.getColumna() + deltaColumna;
        int lon = v.getLongitud();
        char orient = v.getOrientacion();

        // Validar limites en la matriz 6x6
        if (nuevaFila < 0 || nuevaCol < 0)
            return false;
        if (v.getOrientacion() == 'V' && nuevaFila + lon > 6)
            return false;
        if (v.getOrientacion() == 'H' && nuevaCol + lon > 6)
            return false;

        // Verificar si las celdas estan ocupadas
        // Chequeo la celda hacioa donde se mueve para ser eficiente
        int filaChequeo = (deltaFila > 0) ? nuevaFila + lon - 1 : nuevaFila;
        int colChequeo = (deltaColumna > 0) ? nuevaCol + lon - 1 : nuevaCol;

        if (celdas[filaChequeo][colChequeo] != -1 && celdas[filaChequeo][colChequeo] != v.getId()) {
            return false; // Bloqueado por otro auto
        }
        // Ejecutar Movimiento
        actualizarCeldas(v, -1); // Limpiar donde estaba
        v.setPosicion(nuevaFila, nuevaCol); // Actualizar objeto
        actualizarCeldas(v, v.getId()); // Ocupar nuevo lugar
        notifyAll(); // Notificar a otros hilos que la tabla cambio
        return true;
    }

    // metodo esperar recarga
    public synchronized void esperarRecarga(Vehiculo v) throws InterruptedException {
        while (v.getBateria() <= 0 && !simulacionTerminada) {
            wait();
        }
    }

    // Para que los vehiculos notifiquen a los cargadores
    public synchronized void avisarVehiculoSinBateria() {
        notifyAll(); // Despierta a los cargadores que estn esperando
    }

    public synchronized void recargarEnergia() throws InterruptedException {
        while (!simulacionTerminada) {
            boolean alguienRecargado = false;
            for (Vehiculo v : vehiculos.values()) {
                if (v.getBateria() <= 0) {
                    v.setBateria(10);
                    System.out.println("Cargador recargó vehículo " + v.getId());
                    notifyAll(); // Despierta a los vehiculos que esperaban recarga
                    alguienRecargado = true;
                }
            }
            if (alguienRecargado) {
                notifyAll(); // Despertar a los vehiculos que esperaban carga
                return;
            }
            wait(); // No hay nadie que cargar se duerme
        }
    }

    public synchronized boolean simulacionTerminada() {
        return simulacionTerminada;
    }

    public synchronized void terminarSimulacion() {
        simulacionTerminada = true;
        notifyAll(); // despertar hilos que puedan estar esperando
    }

}
