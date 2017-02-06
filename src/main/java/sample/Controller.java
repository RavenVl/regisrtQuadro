package main.java.sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import javafx.application.Platform;
import java.awt.*;
import java.awt.Event;
import java.io.*;
import java.util.*;

public class Controller {

    public ChoiceBox nameCom;
    public Button stopCom;
    public Button ttmpBtn;
    public CheckBox regimCom;
    private  SerialPort serialPort;

    public Button btnAdd;
    public Button btnRemove;
    public Button btnRemoveall;
    public Button btnWork;
    public ListView listCodes;
    public ListView listFile;
    public ListView listEror;
    public Label labelCHcod;
    public Button btnRead;
    public TextField markaTxt;
    public Button startCom;
    public String portData="";

    ObservableList<File> listF = FXCollections.observableList(new ArrayList<>());
    ObservableList<String> listS =FXCollections.observableList(new ArrayList<>());
    ObservableList<String> listE =FXCollections.observableList(new ArrayList<>());
    TreeSet<String> strCode = new TreeSet();
    String[] serialPortList;
    @FXML
//private Button btnAdd;

    private  void initialize(){
        //listFile.setStyle("-fx-font:  bold italic 12pt Lucida Console;");
        listCodes.setStyle("-fx-font:  bold italic 8pt 'Lucida Console';");
        listEror.setStyle("-fx-font:  bold italic 8pt 'Lucida Console';");
        labelCHcod.setStyle("-fx-font:  bold italic 14pt 'Lucida Console';");
        labelCHcod.setText("");
        //markaTxt.setEditable(false);
        markaTxt.setDisable(true);
        serialPortList = SerialPortList.getPortNames();
        ObservableList<String> nC = FXCollections.observableList(Arrays.asList(serialPortList));
        nameCom.setItems(nC);
        stopCom.setDisable(true);
        ttmpBtn.setVisible(false);
        nameCom.getSelectionModel().selectLast();

    }

    public void btnAddClick(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        Stage primaryStage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        /*if (selectedFile != null) {
            primaryStage.display(selectedFile);
        }*/

        if (selectedFile!=null) {
            listF.add(selectedFile);
        }


        listFile.setItems(listF);


    }

    public void btnRemoveAll(ActionEvent actionEvent) {

        strCode.removeAll(strCode);
        listF.removeAll(listF);
        listS.removeAll(listS);
        listE.removeAll(listE);
        labelCHcod.setText(" ");
        //listFile.setItems(listF);
    }

    public void btnRemoveclick(ActionEvent actionEvent) {
        //File selectedFile = (File)listFile.getSelectionModel().getSelectedItems();
        File selectedFile =(File) listFile.getSelectionModel().getSelectedItem();
      listF.remove(selectedFile);
        listFile.setItems(listF);
    }

    public void btnWorkclick(ActionEvent actionEvent) throws Exception {

        BufferedReader reader;
        //strCode = new TreeSet<>();
        for (File file : listF) {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String temp = "";

            while((temp = reader.readLine())!= null){
                for (int i = 0; i <temp.length() ; i+=34) {
                    String temp1= temp.substring(i,i+34);
                    strCode.add(temp1);
                }
            }
            reader.close();

        }
        listS.removeAll(listS);

        listS.addAll(strCode);
        listCodes.setItems(listS);
        labelCHcod.setText("Штрих кодов  - "+listS.size());

        ProverkaPropuskov();


    }

    private void ProverkaPropuskov() {
        listE.removeAll(listE);
        for (int i = 1; i < listS.size(); i++) {


            if (getCode(listS.get(i))-getCode(listS.get(i-1))>1 || getCode(listS.get(i))-getCode(listS.get(i-1))<-1){
                String temp = getCode(listS.get(i-1))+"---" +getCode(listS.get(i));
                listE.add(temp);

            }
        }
        listEror.setItems(listE);
    }

    int getCode(String code){
        return Integer.parseInt(code.substring(11,16));
    }

    public void btnSaveClick(ActionEvent actionEvent) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        Stage primaryStage = new Stage();
        File selectedFile = fileChooser.showSaveDialog(primaryStage);


        PrintWriter writer = null;
        if (selectedFile!=null) {
            writer = new PrintWriter(selectedFile);
            for (String s : listS) {
                writer.println(s);
            }
            writer.flush();
            writer.close();
        }


    }

   


    public void btnReadPress(ActionEvent actionEvent) {
       // markaTxt.setEditable(true);
        if (btnRead.getText().equals("Читать")){
            markaTxt.setDisable(false);
            markaTxt.requestFocus();
            markaTxt.selectAll();
            btnRead.setText("Стоп");
        }
        else{
            markaTxt.setDisable(true);
            btnRead.setText("Читать");
            ProverkaPropuskov();
        }


    }




    public void markaKeyEnter(ActionEvent actionEvent) {
        //labelCHcod.setText(markaTxt.getText());
        strCode.add(markaTxt.getText());
        listS.removeAll(listS);
        listS.addAll(strCode);
        listCodes.setItems(listS);
        labelCHcod.setText("Штрих кодов  - "+listS.size());
        ProverkaPropuskov();
        markaTxt.selectAll();

    }

    public void startComClick(ActionEvent actionEvent) {
        String nameComPort = nameCom.getValue().toString();
        //System.out.println(nameComPort);
        serialPort = new SerialPort(nameComPort);
        startCom.setDisable(true);
        stopCom.setDisable(false);
        try {
            //Открываем порт
            serialPort.openPort();
            //Выставляем параметры
            serialPort.setParams(SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //Включаем аппаратное управление потоком
            //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
            //        SerialPort.FLOWCONTROL_RTSCTS_OUT);
            //Устанавливаем ивент лисенер и маску
            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
            //Отправляем запрос устройству
            //serialPort.writeString("Get data");
        }
        catch (SerialPortException ex) {
            System.out.println("Erorr inicialisation port");
        }
    }

    public void stopComClick(ActionEvent actionEvent) {

        startCom.setDisable(false);
        stopCom.setDisable(true);
        try {
            serialPort.closePort();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }

    }

    public void tmpBtnClick(ActionEvent actionEvent) {

        //System.out.println(portData);

        if (!regimCom.isSelected()) {
            for (int i = 0; i < portData.length(); i += 34) {
                String temp1 = portData.substring(i, i + 34);
                strCode.add(temp1);
            }
            //strCode.add(portData);
            listS.removeAll(listS);
            listS.addAll(strCode);
            listCodes.setItems(listS);
            listCodes.refresh();
            //listS.add(data);
            labelCHcod.setText("Штрих кодов  - " + listS.size());
            ProverkaPropuskov();
        }
        else {
            strCode.add(portData);
            listS.removeAll(listS);
            listS.addAll(strCode);
            listCodes.setItems(listS);
            listCodes.refresh();
        }

    }

    private  class PortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    String data = serialPort.readString(event.getEventValue());
                    portData = data;
                    Platform.runLater(() -> javafx.event.Event.fireEvent(ttmpBtn, new ActionEvent()));

                    //И снова отправляем запрос
                    //serialPort.writeString("Get data");




                }
                catch (SerialPortException ex) {
                    System.out.println("Error read port");
                }
            }
        }
    }


}
