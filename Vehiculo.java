public class Vehiculo implements Runnable {
    private final int id;
    private final char orientacion; // Aqui vemos si es 'H' o 'V'
    private int fila;
    private int columna;
    private final int longitud;
    private int bateria;
    private Estacionamiento estacionamiento;
    private volatile boolean activo = true;

    public Vehiculo(int id, char orientacion, int fila, int columna, int longitud, int bateria) {
        this.id = id;
        this.orientacion = orientacion;
        this.fila = fila;
        this.columna = columna;
        this.longitud = longitud;
        this.bateria = bateria;
    }

    public void setEstacionamiento(Estacionamiento estacionamiento) {
        this.estacionamiento = estacionamiento;
    }

    // los gets y sets
    public int getId() {
        return id;
    }
    public char getOrientacion() {
        return orientacion;
    }
    public int getFila() {
        return fila;
    }
    public int getColumna() {
        return columna;
    }
    public int getLongitud() {
        return longitud;
    }
    public int getBateria() {
        return bateria;
    }
    public void setPosicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public void setBateria(int bateria) {
        this.bateria = bateria;
    }

    @Override
    public void run() {
        // Heuristica y mvimiento
        System.out.println("Vehiculo " + id + " iniciado");
        try {
            while (activo && !estacionamiento.simulacionTerminada()) {
                // gestion de bateria
                if (bateria <= 0) {
                    System.out.println("Vehiculo " + id + " sin batería, esperando recarga");
                    estacionamiento.avisarVehiculoSinBateria();
                    estacionamiento.esperarRecarga(this);
                    // se sale del wait, ya tiewne bateria
                }

                // decidir movimiento: siempre hacia adelante según orientación
                int direcc = (Math.random() > 0.5) ? 1 : -1;
                int df = (orientacion == 'V') ? direcc : 0;
                int dc = (orientacion == 'H') ? direcc : 0;

                if (estacionamiento.moverVehiculo(this, df, dc)) {
                    this.bateria--;// Verificar si es el objetivo y salio
                    System.out.println("Vehiculo " + id + " se desplazó. Batería: " + bateria);
                    if (id == 0 && columna + longitud >= 6 && orientacion == 'H') {
                        System.out.println("Vehiculo 0 se salio del estacionamiento");
                        estacionamiento.terminarSimulacion();
                        break;
                    }
                } else {
                    // si no se puede mover, esperamos un tiempo aleatorio.
                    Thread.sleep(150 + (int) (Math.random() * 300));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Vehiculo " + id + " finalizado.");
    }
}
