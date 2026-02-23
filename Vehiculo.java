public class Vehiculo implements Runnable {
    private final int id;
    private final char orientacion; // 'H' o 'V'
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
        System.out.println("Vehiculo " + id + " iniciado.");
        while (!estacionamiento.simulacionTerminada() && activo) {
            try {
                // Si no hay batería, esperar recarga
                if (bateria == 0) {
                    System.out.println("Vehiculo " + id + " sin batería, esperando recarga...");
                    estacionamiento.avisarVehiculoSinBateria(); // agrega la notificacion
                    estacionamiento.esperarRecarga(this);
                    // Al despertar, la batería ya debería ser >0
                }

                // Decidir movimiento: siempre hacia adelante según orientación
                int deltaFila = (orientacion == 'V') ? 1 : 0;
                int deltaColumna = (orientacion == 'H') ? 1 : 0;

                // Intentar mover
                boolean movio = estacionamiento.moverVehiculo(this, deltaFila, deltaColumna);
                if (movio) {
                    bateria--;
                    System.out.println(
                            "Vehiculo " + id + " se movió a (" + fila + "," + columna + "), batería: " + bateria);
                } else {
                    // Si no se pudo mover (por límite o porque no había espacio), esperar un poco
                    Thread.sleep(100);
                }

                // Verificar si es el objetivo y salió
                if (id == 0 && columna + longitud == 6) {
                    System.out.println("Vehiculo objetivo salió del estacionamiento.");
                    activo = false;
                    estacionamiento.terminarSimulacion(); // opcional, pero ya se llama desde Main
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Vehiculo " + id + " finalizado.");
    }
}