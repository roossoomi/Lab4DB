package org.example;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GUI extends JFrame {
    private File selectedFile;
    private Manager calculation;

    public GUI() {
        setTitle("Реакторы");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel jLabel = new JLabel("<html>Добрый день!<br> Данная программа позволит Вам<br> ознакомиться с данными с сайта <br> PRIS.</html>");
        jLabel.setBounds(100, 10, 220, 60);


        JButton chooseDatabaseButton = new JButton("Выбрать базу данных");
        chooseDatabaseButton.setBounds(100, 100, 220, 30);

        JButton calculateCountryButton = new JButton("Рассчитать для стран");
        calculateCountryButton.setBounds(100, 150, 220, 30);

        JButton calculateRegionButton = new JButton("Рассчитать для регионов");
        calculateRegionButton.setBounds(100, 200, 220, 30);

        JButton calculateCompanyButton = new JButton("Рассчитать для компаний");
        calculateCompanyButton.setBounds(100, 250, 220, 30);

        JButton goodBye = new JButton("Завершение работы");
        goodBye.setBounds(100, 300, 220, 30);

        add(jLabel);
        add(chooseDatabaseButton);
        add(calculateCountryButton);
        add(calculateRegionButton);
        add(calculateCompanyButton);
        add(goodBye);

        calculation = new Manager();
        chooseDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
                int result = fileChooser.showOpenDialog(GUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    if (selectedFile != null && selectedFile.exists()) {
                        System.out.println("Выбранный файл базы данных: " + selectedFile.getAbsolutePath());
                    } else {
                        System.out.println("Файл базы данных не найден.");
                    }
                }
            }
        });
        goodBye.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        });


        calculateCountryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    executeCalculation("Считывание базы данных...", "countries_consumption.xlsx", workbook -> {
                        calculation.CalculateForCountries(selectedFile.getAbsolutePath(), workbook);
                    });
                } else {
                    System.out.println("Файл базы данных не выбран.");
                }
            }
        });

        calculateRegionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    executeCalculation("Считывание базы данных...", "regions_consumption.xlsx", workbook -> {
                        calculation.CalculateForRegion(selectedFile.getAbsolutePath(), workbook);
                    });
                } else {
                    System.out.println("Файл базы данных не выбран.");
                }
            }
        });

        calculateCompanyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    executeCalculation("Считывание базы данных...", "companies_consumption.xlsx", workbook -> {
                        calculation.CalculateForCompany(selectedFile.getAbsolutePath(), workbook);
                    });
                } else {
                    System.out.println("Файл базы данных не выбран.");
                }
            }
        });
    }



    private void executeCalculation(String message, String outputFileName, CalculationTask task) {
        JFrame progressFrame = createProgressFrame(message);
        new Thread(() -> {
            try (Workbook workbook = new XSSFWorkbook()) {
                task.execute(workbook);
                String outputPath = System.getProperty("user.dir") + System.getProperty("file.separator")+ outputFileName;
                try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                    workbook.write(fileOut);
                    System.out.println(outputPath);

                }
            } catch (SQLException | IOException ep) {
                ep.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(progressFrame::dispose);
            }
        }).start();
    }

    private JFrame createProgressFrame(String message) {
        JFrame progressFrame = new JFrame(message);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JLabel label = new JLabel(message, JLabel.CENTER);
        progressFrame.setLayout(new BorderLayout());
        progressFrame.add(label, BorderLayout.NORTH);
        progressFrame.add(progressBar, BorderLayout.CENTER);
        progressFrame.setSize(300, 100);
        progressFrame.setLocationRelativeTo(null);
        progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        progressFrame.setVisible(true);
        return progressFrame;
    }

    interface CalculationTask {
        void execute(Workbook workbook) throws SQLException, IOException;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
