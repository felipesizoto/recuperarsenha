import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class RecuperarSenhaTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/RecuperarSenha.csv", numLinesToSkip = 1, delimiter = ',')
    public void testRecuperarSenha(String email, String tipoTeste) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get("https://iterasys.learnworlds.com/home");

        try {
            // Abrir modal recuperar senha
            WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[text()='Sign in']")));
            signInBtn.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("animatedModal")));

            WebElement esqueceuSenha = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//a[contains(@class,'learnworlds-main-text-very-small') and text()='Esqueceu sua senha?']")));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", esqueceuSenha);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", esqueceuSenha);

            // Digitar email com delay
            WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@type='email' and @name='email' and contains(@class,'js-reset-pass-input')]")));

            emailInput.click();
            emailInput.clear();
            for (char c : email.toCharArray()) {
                emailInput.sendKeys(Character.toString(c));
                Thread.sleep(100);
            }

            // Clicar Ok
            WebElement okBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class, 'reset-pass-btn')]//span[text()='Ok']")));
            okBtn.click();

            // Validações por tipo de teste
            switch (tipoTeste) {
                case "positivo":
                    WebElement continuarBtn = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[contains(@class, 'js-animated-close') and contains(text(),'continuar')]")));
                    continuarBtn.click();
                    break;

                case "nao_cadastrado":
                    boolean msgErroPresente = driver.findElements(
                            By.xpath("//p[contains(text(),'E-mail não cadastrado')]")).size() > 0;

                    if (!msgErroPresente) {
                        System.out.println("⚠ BUG DETECTADO: Sistema não exibiu mensagem de 'E-mail não cadastrado'!");
                    }
                    assertTrue(msgErroPresente, "BUG: Sistema não exibiu mensagem de 'E-mail não cadastrado'!");
                    break;

                case "invalido":
                    WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//p[contains(@class,'js-reset-pass-error-msg') and text()='Este campo é obrigatório.']")));
                    assertTrue(errorMsg.isDisplayed());
                    break;

                default:
                    throw new IllegalArgumentException("Tipo de teste desconhecido: " + tipoTeste);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Teste interrompido: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }
}
