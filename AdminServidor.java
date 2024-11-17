import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminServidor {

    private static JTextArea areaMensajes;
    public static ExecutorService threadPool = Executors.newFixedThreadPool(1000);

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Administración Servidor creada por Hugo González Ruiz");
        JLabel label = new JLabel("Puerto:");
        JTextField textfield = new JTextField();
        JButton botón = new JButton("Iniciar servidor");
        
        // JTextArea para mostrar los mensajes, configurado como solo lectura
        areaMensajes = new JTextArea();
        areaMensajes.setEditable(false); // No permitir edición del texto
        areaMensajes.setPreferredSize(new Dimension(600, 300)); // Ajusta el tamaño del área

        // Añadir el JScrollPane para permitir el desplazamiento
        JScrollPane scrollPane = new JScrollPane(areaMensajes);
        scrollPane.setPreferredSize(new Dimension(600, 300)); // Ajustar el tamaño del JScrollPane

        // Configuración de la ventana
        frame.requestFocusInWindow();
        frame.setVisible(true);
        frame.setSize(660, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.YELLOW);
        frame.setLayout(new FlowLayout());
        label.setPreferredSize(new Dimension(123, 80));
        textfield.setPreferredSize(new Dimension(123, 80));
        botón.setPreferredSize(new Dimension(123, 80));
        frame.add(label);
        frame.add(textfield);
        frame.add(botón);
        frame.add(scrollPane); // Añadir JScrollPane en lugar de JTextArea directamente

        // Acción al pulsar el botón para iniciar el servidor
        botón.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int puerto = Integer.parseInt(textfield.getText());
                
                // Crea y lanza un hilo independiente para el servidor
                new Thread(() -> iniciarServidor(puerto)).start();
            }
        });
    }

    // Método para iniciar el servidor en un hilo independiente
    private static void iniciarServidor(int puerto) {
        try {
            ServerSocket servidor = new ServerSocket(puerto);
            actualizarMensaje("Servidor iniciado en el puerto: "+ puerto);
            while (true) {
                Socket cliente = servidor.accept();
                InetAddress inetcliente = cliente.getInetAddress();
                actualizarMensaje("Un nuevo cliente se ha conectado: " + inetcliente.getHostAddress() + ": " + inetcliente.getHostName());

                // Ejecuta el manejador del cliente en un hilo separado
                threadPool.execute(new ClientHandler(cliente));
            }

        } catch (IOException e1) {
            actualizarMensaje("Hubo un error al iniciar el servidor: " + e1.getMessage());
        }
    }

    // Método para actualizar el JTextArea de mensajes
    public static void actualizarMensaje(String mensaje) {
        // Usamos SwingUtilities.invokeLater para asegurar que la actualización del JTextArea se haga en el hilo EDT
        SwingUtilities.invokeLater(() -> {
            areaMensajes.append(mensaje + "\n"); // Añade el mensaje al final del área de texto
            areaMensajes.setCaretPosition(areaMensajes.getDocument().getLength()); // Desplazar hacia abajo
        });
    }
}

class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("Cliente conectado desde: " + socket.getInetAddress().getHostAddress());

            String inputLine;
            while ((inputLine = entrada.readLine()) != null) {
                System.out.println("Mensaje recibido: " + inputLine); // Traza en consola

                // Llamamos al método actualizarMensaje desde la clase AdminServidor
                AdminServidor.actualizarMensaje("Mensaje de cliente: " + inputLine);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Hubo un error al leer los datos del cliente.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}