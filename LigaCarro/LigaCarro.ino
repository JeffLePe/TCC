#include <AltSoftSerial.h>
#include <LiquidCrystal_I2C.h>
#include <FPS_GT511C3.h>

//LiquidCrystal lcd(12, 11, 5, 4, 3, 2);
LiquidCrystal_I2C lcd(0x20, 16, 2);

AltSoftSerial bluetooth(8, 9); //TX/RX (bluetooth)                                                                                                                                                                        
char incomingByte;
int ligado = 0;
const int relePartida = 11;
const int releIgnicao = 10;
int temp = 0;
FPS_GT511C3 fps(6, 7); //TX/RX (scanner)
const int porta = 4;
int estadoPorta = 0;
int portaAberta = 0;
const int cambio = 3;
int estadoCambio = 0;

void setup() {

  lcd.init();
  lcd.backlight();
  pinMode(relePartida, OUTPUT);
  pinMode(releIgnicao, OUTPUT);
  bluetooth.begin(9600);
  fps.Open();
  fps.SetLED(true);
  pinMode(porta, INPUT);
  pinMode(cambio, INPUT);

}

//Funcao responsavel por cadastar impressoes digitais
void Enroll() 
{
  int enrollid = 0;
  //É verificado quantos digitais tem cadastradas, e selecionandoa proxima posição disponivel para armazenamento
  //======================================================================================
  bool usedid = true;
  while (usedid == true)
  {
    usedid = fps.CheckEnrolled(enrollid);
    if (usedid==true) enrollid++;
  }
  fps.EnrollStart(enrollid);
  temp=enrollid;
  //======================================================================================

  //Aguarda até que o dedo seja retirado do scanner
  while(fps.IsPressFinger() == false) delay(100);
  bool bret = fps.CaptureFinger(true);
  int iret = 0;
  //Se conseguir realizar alguma leitura a variavel "bret" valerá 1
  if (bret != false)
  {
    //Se for diferente de 0 irá printar a informação para que o dedo seja removido do scanner
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Remova o dedo");
    fps.Enroll1();//Salva a primeira informacao da digital
    //Aguarda até que o dedo seja retirado do scanner
    while(fps.IsPressFinger() == true) delay(100);
    lcd.clear();//Limpa o buffer do LCD
    //Solicita que o dedo seja pressionado no scanner novamente
    lcd.setCursor(0, 0);
    lcd.print("Pressione o dedo");
    lcd.setCursor(0, 1);
    lcd.print("Novamente");
    //Aguarda até que o dedo seja retirado do scanner
    while(fps.IsPressFinger() == false) delay(100);

    //Repete tudo por mais 2 vezes
    //=====================================================================================
    bret = fps.CaptureFinger(true);
    if (bret != false)
    {
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Remova o dedo");
      fps.Enroll2();
      while(fps.IsPressFinger() == true) delay(100);
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Pressione o dedo");
      lcd.setCursor(0, 1);
      lcd.print("Novamente");
      while(fps.IsPressFinger() == false) delay(100);
      //====================================================================================
      //====================================================================================
      bret = fps.CaptureFinger(true);
      if (bret != false)
      {
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("Remova o dedo");
        iret = fps.Enroll3();
        //===================================================================================
        //Nessa parte é verificado se se o cadastro foi realizado com sucesso
        enrollid=0;          
        usedid = true;
        while (usedid == true)
        {
          usedid = fps.CheckEnrolled(enrollid);
          if (usedid==true) enrollid++;
        } 
        //Compara se o dedo foi pressionado 3 vezes e se a posição anterior da memoria é igualk a atual
        if (iret == 0&&temp!=enrollid)
        {
          //Se for igual printará as informações no LCD e salva automaticamente na memória
          lcd.clear();//Limpa o buffer do LCD
          lcd.setCursor(0, 0);
          lcd.print("Cadastro feito");
          lcd.setCursor(0, 1);
          lcd.print("Com sucesso");
          lcd.setCursor(13, 1);
          lcd.print(enrollid);
          delay(1000);//Aguarda 1 segundo
        }
        else
        {
          //Se não printará uma informação acusando falha
          lcd.clear();//Limpa o buffer do LCD
          lcd.setCursor(0, 0);
          lcd.print("Cadastro falho");
          lcd.setCursor(0, 1);
          lcd.print("Tente novamente");
          delay(1000);//Aguarda 1 segundo

        }
      }
      // Se houver falhas durante a captura printará umas das 3 mensagens abaixo, de acordo com cada posição
      //Erro 1
      else {
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("Falha na captura");
        lcd.setCursor(0, 1);
        lcd.print("Do primeiro dedo");
        delay(1000);
      }
      //====================================================================================
    }
    //Erro 2
    else {
      lcd.clear();
      lcd.setCursor(0, 0); //Posiciona o cursor na primeira coluna(0) e na primeira linha(0) do LCD
      lcd.print("Falha na captura"); //Escreve no LCD "Olá Garagista!"
      lcd.setCursor(0, 1); //Posiciona o cursor na primeira coluna(0) e na primeira linha(0) do LCD
      lcd.print("Do segundo dedo");
      delay(1000);
    }
    //=====================================================================================
  }
  //Erro 3
  else {
    lcd.clear();
    lcd.setCursor(0, 0); //Posiciona o cursor na primeira coluna(0) e na primeira linha(0) do LCD
    lcd.print("Falha na captura"); //Escreve no LCD "Olá Garagista!"
    lcd.setCursor(0, 1); //Posiciona o cursor na primeira coluna(0) e na primeira linha(0) do LCD
    lcd.print("Do terceiro dedo");
    delay(1000);
  }
  //======================================================================================
}

void loop() {

  estadoCambio = digitalRead(cambio);

  if(ligado == 0) {

    lcd.setCursor(0,0);
    lcd.print("Insira sua");
    lcd.setCursor(0,1);
    lcd.print("Digital");
    portaAberta = 0;
    
    if(bluetooth.available() > 0) {
      incomingByte = char(bluetooth.read());
      if(incomingByte == 'C') {
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Novo Cadastro");
        lcd.setCursor(0,1);
        lcd.print("Aguarde!");
        delay(2000);
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Posicione o dedo");
        lcd.setCursor(0, 1);
        lcd.print("no Scanner");
        delay(3000);
        fps.Open();
        Enroll();
        lcd.clear();
      }

      if(incomingByte == 'D') {
        fps.Open();
        fps.DeleteAll();
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Registro Apagado");
        delay(3000);
        lcd.clear();
      }

      if(incomingByte == 'H' && estadoCambio != 0) {
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Ligando Carro!");
        digitalWrite(releIgnicao, HIGH);
        delay(3000);
        digitalWrite(relePartida, HIGH);
        delay(1000);
        digitalWrite(relePartida, LOW);
        ligado = 1;
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Carro Ligado!");
        bluetooth.println("Carro Ligado!");
        delay(2000);
        lcd.clear();
      }
    }
    if(fps.IsPressFinger() && estadoCambio != 0) {
      fps.CaptureFinger(false);
      int id = fps.Identify1_N();
      if(id < 20) {
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Ligando Carro!");
        digitalWrite(releIgnicao, HIGH);
        delay(3000);
        digitalWrite(relePartida, HIGH);
        while(fps.IsPressFinger()) { }
        digitalWrite(relePartida, LOW);
        ligado = 1;
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Carro Ligado!");
        delay(2000);
        lcd.clear();
      }
      else {
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Partida nao");
        lcd.setCursor(0,1);
        lcd.print("permitida");
        delay(2000);
        lcd.clear();
      }
      
    }
    
  }
  else {
    lcd.setCursor(0,0);
    lcd.print("Para desligar");
    lcd.setCursor(0,1);
    lcd.print("insira digital");
    estadoPorta = digitalRead(porta);
    if(fps.IsPressFinger()) {
      fps.CaptureFinger(false);
      int id = fps.Identify1_N();
      if(id < 20) {
        digitalWrite(releIgnicao, LOW);
        ligado = 0;
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Carro desligado!");
        bluetooth.println("Carro desligado!");
        delay(2000);
        lcd.clear();
      }
      else {
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Desligamento nao");
        lcd.setCursor(0,1);
        lcd.print("permitido");
        delay(2000);
        lcd.clear();
      }
    }
    if(bluetooth.available() > 0) {
      incomingByte = char(bluetooth.read());
      if(incomingByte == 'L') {
        digitalWrite(releIgnicao, LOW);
        ligado = 0;
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Carro desligado!");
        bluetooth.println("Carro desligado!");
        delay(1000);
        lcd.clear();
      }
    }
    if(estadoPorta == 1 || portaAberta == 1) {
      portaAberta = 1;
      lcd.clear();
      lcd.setCursor(0,0);
      lcd.print("Identifique-se");
      delay(5000);
      if(fps.IsPressFinger()) {
        fps.CaptureFinger(false);
        int id = fps.Identify1_N();
        if(id < 20) {
          portaAberta = 0;
          lcd.clear();
          lcd.setCursor(0,0);
          lcd.print("Carro");
          lcd.setCursor(0,1);
          lcd.print("desbloqueado");
          bluetooth.println("Carro desbloqueado!");
          delay(3000);
          lcd.clear();
        }
      }
      if(portaAberta == 1) {
        lcd.clear();
        lcd.setCursor(0,0);
        lcd.print("Carro sera");
        lcd.setCursor(0,1);
        lcd.print("Bloqueado");
        delay(15000);
        digitalWrite(releIgnicao, LOW);
        portaAberta = 0;
        ligado = 0;
        lcd.clear();
      }
    }  
  }
}



