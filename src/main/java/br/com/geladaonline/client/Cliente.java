package br.com.geladaonline.client;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jettison.JettisonFeature;

import br.com.geladaonline.model.Cerveja;
import br.com.geladaonline.model.Cerveja.Tipo;
import br.com.geladaonline.model.rest.Cervejas;

public class Cliente {

    public static void main(String[] args) {
        List<Cerveja> cervejas = recuperaCervejas();
        cervejas.forEach(System.out::println);
        
        Cerveja cervejaRecemCriada = criarCerveja(new Cerveja("Antartica", "Cerveja Antartica", "Ambev", Tipo.WEIZEN));
        System.out.println(cervejaRecemCriada);
    }
    
    public static List<Cerveja> recuperaCervejas() {
        Client client = ClientBuilder.newClient();
        
        Cervejas cervejas = client.target(Constants.HOST)
                .path("cervejas")
                .request(MediaType.APPLICATION_XML)
                .get(Cervejas.class);
        
        List<Cerveja> cervejaList = new ArrayList<>();
        cervejas.getLinks().forEach(link -> {
            Cerveja cerveja = ClientBuilder.newClient()
                    .register(JettisonFeature.class)
                    .invocation(link)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Cerveja.class);
            
            cervejaList.add(cerveja);
        });
        
        return cervejaList;
    }
    
    public static Cerveja criarCerveja(Cerveja cerveja) {
        Response response = ClientBuilder.newClient()
            .target(Constants.HOST)
            .path("cervejas")
            .request()
            .post(Entity.xml(cerveja));
        
        if(response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            Link link = Link.fromUri(response.getLocation()).build();
            
            return ClientBuilder.newClient()
                    .register(JettisonFeature.class)
                    .invocation(link)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Cerveja.class);
        }
        
        return null;
    }
}