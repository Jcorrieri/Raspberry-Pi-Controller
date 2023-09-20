package com.example.app.Controllers;

import com.example.app.App;
import javafx.fxml.FXML;

public class GpioController {

    protected int currentPin = -1;

    @FXML
    public void updateDetails(int pin) {
        currentPin = pin;
        App.getController().selectBcmPin(pin);
    }

    @FXML
    private void bcmPinTwo() { updateDetails(2); }

    @FXML
    private void bcmPinThree() { updateDetails(3); }

    @FXML
    private void bcmPinFour() { updateDetails(4); }

    @FXML
    private void bcmPinFive() { updateDetails(5); }

    @FXML
    private void bcmPinSix() { updateDetails(6); }

    @FXML
    private void bcmPinSeven() { updateDetails(7); }

    @FXML
    private void bcmPinEight() { updateDetails(8); }

    @FXML
    private void bcmPinNine() { updateDetails(9); }

    @FXML
    private void bcmPinTen() { updateDetails(10); }

    @FXML
    private void bcmPinEleven() { updateDetails(11); }

    @FXML
    private void bcmPinTwelve() { updateDetails(12); }

    @FXML
    private void bcmPinThirteen() { updateDetails(13); }

    @FXML
    private void bcmPinFourteen() { updateDetails(14); }

    @FXML
    private void bcmPinFifteen() { updateDetails(15); }

    @FXML
    private void bcmPinSixteen() { updateDetails(16); }

    @FXML
    private void bcmPinSeventeen() { updateDetails(17); }

    @FXML
    private void bcmPinEighteen() { updateDetails(18); }

    @FXML
    private void bcmPinNineteen() { updateDetails(19); }

    @FXML
    private void bcmPinTwenty() { updateDetails(20); }

    @FXML
    private void bcmPinTwentyOne() { updateDetails(21); }

    @FXML
    private void bcmPinTwentyTwo() { updateDetails(22); }

    @FXML
    private void bcmPinTwentyThree() { updateDetails(23); }

    @FXML
    private void bcmPinTwentyFour() { updateDetails(24); }

    @FXML
    private void bcmPinTwentyFive() { updateDetails(25); }

    @FXML
    private void bcmPinTwentySix() { updateDetails(26); }

    @FXML
    private void bcmPinTwentySeven() { updateDetails(27); }
}
