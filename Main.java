import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // AQUI verificamos que se pase el nombre del archivo como argumento
        if (args.length < 1) {
            System.err.println("Uso: java Main <config.txt>");
            System.exit(1);
        }

        String archivo = args[0];
        List<Vehiculo> vehiculos = new ArrayList<>();
        int numCargadores = 0;

        // AQUI el archivo línea por línea
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty())
                    continue; // saltar si hay lineas vacias

                String[] partes = linea.split("\\s+");
                // Si tiene 6 campos es un vehiculo(Validamos que es un vehiculo)
                if (partes.length == 6) {
                    try {
                        int id = Integer.parseInt(partes[0]);
                        char orientacion = partes[1].charAt(0);
                        int fila = Integer.parseInt(partes[2]);
                        int columna = Integer.parseInt(partes[3]);
                        int longitud = Integer.parseInt(partes[4]);
                        int bateria = Integer.parseInt(partes[5]);

                        Vehiculo v = new Vehiculo(id, orientacion, fila, columna, longitud, bateria);
                        vehiculos.add(v);
                    } catch (NumberFormatException e) {
                        System.err.println("Error numérico en línea: " + linea);
                    }
                }
                // Si tiene 1 campo es la cantidad de cargadores
                else if (partes.length == 1) {
                    try {
                        numCargadores = Integer.parseInt(partes[0]);
                    } catch (NumberFormatException e) {
                        System.err.println("Cantidad de cargadores invalida: " + linea);
                    }
                } else {
                    System.err.println("Linea con formato incorrecto: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            System.exit(1);
        }

        // Validamos que haya al menos un vehiculo (el objetivo con ID 0)
        if (vehiculos.isEmpty()) {
            System.err.println("No se encontraron vehiculos en el archivo.");
            System.exit(1);
        }

        // Creamos el monitor del estacionamiento, o sea la matriz 6x6
        Estacionamiento estacionamiento = new Estacionamiento();

        // Registramos cada vehículo en el estacionamiento.
        for (Vehiculo v : vehiculos) {
            estacionamiento.colocarVehiculoInicial(v);
        }

        // Aqui asignamos la referencia al estacionamiento a cada vehiculo
        for (Vehiculo v : vehiculos) {
            v.setEstacionamiento(estacionamiento);
        }

        // Creamos los hilos de los vehiculos
        List<Thread> hilosVehiculos = new ArrayList<>();
        for (Vehiculo v : vehiculos) {
            Thread t = new Thread(v);
            hilosVehiculos.add(t);
        }

        // Creamos y arrancamos los hilos de las unidades de carga
        List<Thread> hilosCargadores = new ArrayList<>();
        for (int i = 0; i < numCargadores; i++) {
            UnidadCarga uc = new UnidadCarga(i, estacionamiento);
            Thread t = new Thread(uc);
            t.start();
            hilosCargadores.add(t);
        }

        // Arrancamos los hilos de los vehiculos
        for (Thread t : hilosVehiculos) {
            t.start();
        }

        // Esperamos a que el vehiculo objetivo (ID 0) termine
        // (los demás vehículos se detendrán cuando el monitor indique fin de
        // simulacion)
        try {
            // Buscamos el hilo del vehiculo con ID 0
            for (int i = 0; i < vehiculos.size(); i++) {
                if (vehiculos.get(i).getId() == 0) {
                    hilosVehiculos.get(i).join(); // esperamos a que termine el objetivo
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Indicar al estacionamiento que la simulación terminó (para que los demás
        // hilos finalicen)
        estacionamiento.terminarSimulacion();

        // Esperar un poco a que los demás hilos reaccionen (opcional)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // ignore
        }

        System.out.println("Simulación finalizada.");
    }
}