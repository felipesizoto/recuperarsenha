import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.Assert.assertTrue;

public class RecuperarSenhaTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get("https://iterasys.learnworlds.com/home");
    }

    private void abrirModalRecuperarSenha() {
        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[text()='Sign in']")));
        signInBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("animatedModal")));

        WebElement esqueceuSenha = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(@class,'learnworlds-main-text-very-small') and text()='Esqueceu sua senha?']")));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", esqueceuSenha);

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", esqueceuSenha);
    }

    private void digitarEmailComDelay(String email) {
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='email' and @name='email' and contains(@class,'js-reset-pass-input')]")));

        emailInput.click();
        emailInput.clear();

        for (char c : email.toCharArray()) {
            emailInput.sendKeys(Character.toString(c));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void clicarOk() {
        WebElement okBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class, 'reset-pass-btn')]//span[text()='Ok']")));
        okBtn.click();
    }

    private void clicarContinuar() {
        WebElement continuarBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class, 'js-animated-close') and contains(text(),'continuar')]")));
        continuarBtn.click();
    }

    @Test
    public void testRecuperarSenhaPositivo() {
        abrirModalRecuperarSenha();
        digitarEmailComDelay("fsizoto@gmail.com");
        clicarOk();
        clicarContinuar();
        // Aqui pode inserir asserts adicionais para sucesso, se quiser
    }

    @Test
    public void testRecuperarSenhaEmailNaoCadastrado() {
        abrirModalRecuperarSenha();
        digitarEmailComDelay("test@test.com.br");
        clicarOk();

        boolean mensagemErroPresente = driver.findElements(
                By.xpath("//p[contains(text(),'E-mail não cadastrado')]")
        ).size() > 0;

        if (!mensagemErroPresente) {
            System.out.println("⚠ BUG DETECTADO: Sistema não exibiu mensagem de 'E-mail não cadastrado'!");
        }

        assertTrue("BUG: Sistema não exibiu mensagem de 'E-mail não cadastrado'!", mensagemErroPresente);
    }

    @Test
    public void testRecuperarSenhaEmailInvalido() {
        abrirModalRecuperarSenha();
        digitarEmailComDelay("felipe");
        clicarOk();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[contains(@class,'js-reset-pass-error-msg') and text()='Este campo é obrigatório.']")));
        assertTrue(errorMsg.isDisplayed());
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
