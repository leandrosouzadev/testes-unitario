package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.LocacaoBuilder.umLocacao;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHojeComDiferencaDias;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	@InjectMocks
	private LocacaoService locacaoService;

	@Mock
	private LocacaoDAO dao;

	@Mock
	private SPCService spcService;

	@Mock
	private EmailService emailService;

	@Rule
	public ErrorCollector errorCollector = new ErrorCollector();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void deveAlugarFilme() throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

		// Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().comValor(5.0).agora());

		// Acao
		Locacao locacao;

		locacao = locacaoService.alugarFilme(usuario, filmes);

		// Verificacao
		errorCollector.checkThat(locacao.getValor(), is(CoreMatchers.equalTo(5.0)));
		errorCollector.checkThat(locacao.getDataLocacao(), ehHoje());
		errorCollector.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));

	}

	@Test(expected = FilmeSemEstoqueException.class)
	public void naoDeveAlugarFilmeSemEstoque() throws Exception {
		// Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilmeSemEstoque().agora());

		// Acao
		locacaoService.alugarFilme(usuario, filmes);
	}

	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
		// Cenario
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		// Acao
		try {
			locacaoService.alugarFilme(null, filmes);
			Assert.fail("Era esperado uma exceção de usuário vazio.");
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuario vazio"));
		}
	}

	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario
		Usuario usuario = umUsuario().agora();

		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio");

		// Acao
		locacaoService.alugarFilme(usuario, null);
	}

	@Test
	public void devePagar75pctNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
				new Filme("Filme 3", 2, 4.0));

		// Acao
		Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

		// Verificacao'
		assertThat(resultado.getValor(), is(11.0));
	}

	@Test
	public void devePagar50pctNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
				new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0));

		// Acao
		Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

		// Verificacao'
		assertThat(resultado.getValor(), is(13.0));
	}

	@Test
	public void devePagar25pctNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
				new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0), new Filme("Filme 4", 2, 4.0));

		// Acao
		Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

		// Verificacao'
		assertThat(resultado.getValor(), is(14.0));
	}

	@Test
	public void devePagar100pctNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0),
				new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0), new Filme("Filme 5", 2, 4.0),
				new Filme("Filme 6", 2, 4.0));

		// Acao
		Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

		// Verificacao'
		assertThat(resultado.getValor(), is(14.0));
	}

	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception {
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

		// Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));			

		// Acao
		Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

		// Verificacao
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}

	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
		// Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));

		when(spcService.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

		// Acao
		try {
			locacaoService.alugarFilme(usuario, filmes);
			// Verificacao

			Assert.fail();
		} catch (LocadoraException e) {
			Assert.assertThat(e.getMessage(), is("Usuario Negativado"));
		}

		verify(spcService).possuiNegativacao(usuario);
	}

	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		// Cenario
		Usuario usuario = umUsuario().agora();
		Usuario usuario2 = umUsuario().comNome("Usuario em dia").agora();
		Usuario usuario3 = umUsuario().comNome("Outra atrasada").agora();

		List<Locacao> locacoes = Arrays.asList(umLocacao().comUsuario(usuario).atrasada().agora(),
				umLocacao().comUsuario(usuario2).agora(), umLocacao().comUsuario(usuario3).atrasada().agora(),
				umLocacao().comUsuario(usuario3).atrasada().agora());

		when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

		// Acao
		locacaoService.notificarAtrasos();

		// Verificacao
		verify(emailService, Mockito.times(3)).notificarAtraso(Mockito.any(Usuario.class));
		verify(emailService).notificarAtraso(usuario);
		verify(emailService, never()).notificarAtraso(usuario2);
		verify(emailService, Mockito.atLeastOnce()).notificarAtraso(usuario3);
		verifyNoMoreInteractions(emailService);
	}

	@Test
	public void deveTratarErroNoSPC() throws Exception {
		// Cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		when(spcService.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrófica"));

		// Verificacao
		exception.expect(LocadoraException.class);
		exception.expectMessage("SPC fora do ar, tente novamente.");

		// Acao
		locacaoService.alugarFilme(usuario, filmes);
	}

	@Test
	public void deveProrrogarUmaLocacao() {
		// Cenario
		Locacao locacao = umLocacao().agora();

		// Acao
		locacaoService.prorrogarLocacao(locacao, 3);
		
		//Verificacao
		ArgumentCaptor<Locacao> argumentCaptor = ArgumentCaptor.forClass(Locacao.class);
		Mockito.verify(dao).salvar(argumentCaptor.capture());
		Locacao locacaoRetornada = argumentCaptor.getValue();
		
		errorCollector.checkThat(locacaoRetornada.getValor(), is(12.0));
		errorCollector.checkThat(locacaoRetornada.getDataLocacao(), ehHoje());
		errorCollector.checkThat(locacaoRetornada.getDataRetorno(), ehHojeComDiferencaDias(3));
	}
//	public static void main(String[] args) {s
//		new BuilderMaster().gerarCodigoClasse(Locacao.class);
//	}	

}
