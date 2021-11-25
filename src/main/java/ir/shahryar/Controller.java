package ir.shahryar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Controller extends Thread {
    private static final String crawlerUrl = "https://www.tradingview.com/markets/cryptocurrencies/prices-all/";
    private static final String cssQuery = "tr.tv-data-table__row.tv-data-table__stroke.tv-screener-table__result-row";
    private static final String clientUrl = "http://localhost:9090/coin/data/in";
    private Document doc;

    public Document getDoc() {
        return doc;
    }

    public void setDoc() throws IOException {
        this.doc = Jsoup.connect(crawlerUrl).get();
    }

    @Override
    public void run() {
        while (true) {
            try {
                setDoc();
                sendData(serialize(ReadData()));
                sleep(10 * 60 * 1000);
            } catch (IOException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private ArrayList<CoinDTO> ReadData() {
        ArrayList<CoinDTO> coinDTOs = new ArrayList<>();
        Elements rows = doc.select(cssQuery);
        for (Element row : rows) {
            Elements tds = row.select("td");
            String coinName = tds.get(0).select("a").html();
            String priceStr = tds.get(3).html().replace(".", "");
            coinDTOs.add(new CoinDTO(coinName, Long.parseLong(priceStr)));
        }
        return coinDTOs;
    }

    private void sendData(String coinDTOs) throws IOException {
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), coinDTOs);
        Request request = new Request.Builder()
                .url(clientUrl)
                .post(body)
                .build();
        Call call = new OkHttpClient().newCall(request);
        Response response = call.execute();
    }

    private String serialize(ArrayList<CoinDTO> o) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(o);
    }
}
