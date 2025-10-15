package com.webcrawler.groovy

import groovyx.net.http.HttpBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.nio.file.Files
import java.nio.file.Paths

static void main(String[] args) {
    task1()
    task2()
    task3()
}

static Document getURLshared(){
    def govAns = HttpBuilder.configure {
        request.uri = 'https://www.gov.br/ans/pt-br'
    }.get()


    def docAns = Jsoup.parse(govAns.toString())
    def linksEncontrados = docAns.select('a[href*="assuntos/prestadores"]')
    def linkEspacoPrestador = ''
    linksEncontrados.each { link ->
        String href = link.attr('href')
        String texto = link.text()

        if(texto == 'Espaço do Prestador de Serviços de Saúde'){
            linkEspacoPrestador = href
        }
    }

    //------------------------------------------------------------------------------------
    def httpEspacoPrestador = HttpBuilder.configure {
        request.uri = linkEspacoPrestador
    }.get()

    def docEspacoPrestador = Jsoup.parse(httpEspacoPrestador.toString())
    def linksEspacoPrestador = docEspacoPrestador.select('a[href*="/assuntos/prestadores/padrao-para-troca"]')
    def linkTissPadrao = ''
    linksEspacoPrestador.each { link ->
        String href = link.attr('href')
        String texto = link.text()

        if(texto == 'TISS - Padrão para Troca de Informação de Saúde Suplementar'){
            linkTissPadrao = href
        }
    }


    //------------------------------------------------------------------------------------
    def httpTissPadrao = HttpBuilder.configure {
        request.uri = linkTissPadrao
    }.get()

    Document docTissPadrao = Jsoup.parse(httpTissPadrao.toString())

    return docTissPadrao
}

static void task1(){

//    println(http3.toString())
    def linksTissPadrao = getURLshared().select('a[href*="setembro-2025"]')

    def linkPadraoTissMesAno = ''
    linksTissPadrao.each { link ->
        String href = link.attr('href')

        linkPadraoTissMesAno = href

    }

    def httpPadraoTissMesAno = HttpBuilder.configure {
        request.uri = linkPadraoTissMesAno
    }.get()

    def docPadraoTissMesAno = Jsoup.parse(httpPadraoTissMesAno.toString())
    def tabela = docPadraoTissMesAno.select('table')
    def tbody = tabela.select('tbody')

    tbody.select('tr').each { tr ->
        tr.select('td').each{td->
            String href = td.select('a').attr('href')
            String texto = td.text()


            if(texto == 'Baixar Componente de Comunicação.(.zip)'){
                byte[] arquivoBaixado = HttpBuilder.configure {
                    request.uri = href
                }.get()

                String nomeArquivo = href.split('/').last()
                File destino = new File('/home/joaopedro/Downloads', nomeArquivo)

                destino.bytes = arquivoBaixado

                println "Arquivo baixado: ${destino.absolutePath}"
            }
        }


    }
}

static void task2() {
    def linksTissPadrao = getURLshared().select('a[href*="Componentes"]')
    def linkPadraoTissHistorico = ''
    linksTissPadrao.each { link ->
        String href = link.attr('href')
        String texto = link.text()
        if (texto == 'Clique aqui para acessar todas as versões dos Componentes')
            linkPadraoTissHistorico = href

    }

    def httpPadraoTissHistorico = HttpBuilder.configure {
        request.uri = linkPadraoTissHistorico
    }.get()

    def docPadraoTissHistorico = Jsoup.parse(httpPadraoTissHistorico.toString())
    def tabela = docPadraoTissHistorico.select('table')
    def tbody = tabela.select('tbody')
    List<String> result= new ArrayList<>()
    result.add("\n")
    result.add("Competência | Publicação | início de vigência")
    result.add("\n")
    tbody.select('tr').eachWithIndex { tr, int i ->
        int flag = 0
        String item = ""

        tr.select('td').eachWithIndex { td, int index ->
            String texto = td.text()

            if(index==0 && (texto.tokenize('/')[1]).toInteger() >=2016){
                flag =1
            }
            if (index>=0 && index<=2 && flag == 1){
                switch (index){
                    case 0:
                        item += texto+' | '
                        break;
                    case 1:
                        item += texto+' | '
                        break
                    case 2:
                        item += texto
                        break
                }
            }
        }
        result.add(item)
    }

    result.each {println(it)}

}

static Void task3(){
    def linksTissPadrao = getURLshared().select('a[href*="tabelas-relacionadas"]')
    def linkPadraoTissTabelas = ''
    linksTissPadrao.each { link ->
        String href = link.attr('href')
        String texto = link.text()
        if (texto == 'Clique aqui para acessar as planilhas')
            linkPadraoTissTabelas = href

    }

    def httpPadraoTissTabelas= HttpBuilder.configure {
        request.uri = linkPadraoTissTabelas
    }.get()

    def docPadraoTissTabelas = Jsoup.parse(httpPadraoTissTabelas.toString())
    def tabela = docPadraoTissTabelas.select('a[href*="Tabelaerrosenvioparaanspadraotiss__1_.xlsx"]')
    String href = tabela.attr('href')

    byte[] arquivoXlsxBaixado = HttpBuilder.configure {
        request.uri = href
    }.get()

    String nomeArquivo = href.split('/').last()
    File destino = new File('/home/joaopedro/Downloads', nomeArquivo)

    destino.bytes = arquivoXlsxBaixado

    println "Arquivo baixado: ${destino.absolutePath}"
}
