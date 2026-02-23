public class UnidadCarga implements Runnable {
    private final int id;
    private final Estacionamiento estacionamiento;
    private volatile boolean activo = true;

    public UnidadCarga(int id, Estacionamiento estacionamiento) {
        this.id = id;
        this.estacionamiento = estacionamiento;
    }

    @Override
    public void run() {
        System.out.println("Unidad de Carga " + id + " iniciada.");
        while (!estacionamiento.simulacionTerminada() && activo) {
            try {
                estacionamiento.recargarEnergia();
                // Pequeña pausa para no saturar el monitor
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Unidad de Carga " + id + " finalizada.");
    }
}