import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class GUI extends JFrame {
    private JTextArea programInput;
    private JTextArea output;
    private JButton executeButton;

    public GUI() {
        initComponents();
    }

    private void initComponents() {

        programInput = new JTextArea(20, 60);
        JScrollPane programScrollPane = new JScrollPane(programInput);

        output = new JTextArea(20, 60);
        output.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(output);


        executeButton = new JButton("Execute");
        executeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeProgram();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(programScrollPane);
        panel.add(executeButton);
        panel.add(outputScrollPane);

        setContentPane(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
    }

    private void executeProgram() {
        String program = programInput.getText();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/gui-program.txt"))) {
            writer.write(program);
            System.out.println("Content successfully written to the file.");
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }


        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);


        CPU cpu = new CPU("src/gui-program.txt");


        System.out.flush();
        System.setOut(oldOut);


        String result = baos.toString();
        output.setText(result);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
}