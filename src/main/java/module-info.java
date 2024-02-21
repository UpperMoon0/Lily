module com.example.aelia {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires client.sdk;
    requires com.azure.ai.translation.text;
    requires ch.qos.logback.classic;
    requires org.apache.opennlp.tools;
    requires java.desktop;


    opens com.nhat.lily to javafx.fxml;
    exports com.nhat.lily;
    exports com.nhat.lily.controllers;
    opens com.nhat.lily.controllers to javafx.fxml;
}