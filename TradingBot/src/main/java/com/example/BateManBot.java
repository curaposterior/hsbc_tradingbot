package com.example;

import com.example.model.order.Instrument;
import com.example.model.order.ProcessedOrder;
import com.example.model.order.SubmittedOrder;
import com.example.model.rest.*;
import com.example.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sound.sampled.Port;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.example.model.rest.PortfolioResponse.*;

public class BateManBot implements Runnable {
    private final Platform platform;
    private static final Logger logger = LoggerFactory.getLogger(TradingBot.class);
    public BateManBot(Platform platform) {
        this.platform = platform;
    }

    public Portfolio getPortfolio() {
        var prt = platform.portfolio();
        switch (prt) {
            case PortfolioResponse.Portfolio x -> {
                return x;
            }
            case PortfolioResponse.Other y -> {
                logger.info("{}", y.restResponse());
            }
        }
        return null;
    }

    public void buyInstrument(Portfolio p, Instrument i, long qty, long price) {
        platform.submit(new SubmitOrderRequest.Buy(
                i.symbol(), UUID.randomUUID().toString(), qty, price
        ));
    }

    public void sellInstrument(Portfolio p,Instrument i, long qty, long price) {

        for (var iter: p.portfolio()) {
            if (iter.instrument().symbol().equals(i.symbol())) {
                if (iter.qty() > 0 && iter.qty()-qty > 0) {
                    var x = platform.submit(new SubmitOrderRequest.Sell(
                            i.symbol(), UUID.randomUUID().toString(), qty, price
                    ));
                    logger.info("{}", x);
                    return;
                }
            }
        }
    }

    public long getCash() {
        var sub = platform.portfolio();
        switch (sub) {
            case PortfolioResponse.Portfolio x -> {
                return x.cash();
            }
            case PortfolioResponse.Other y -> {
                logger.info("{}", y.restResponse());
            }
        }

        logger.info("{}", sub);
        return 0;
    }

    public Map<String, Long> getPortfolioInstruments() {
        Map<String, Long> listOfInstruments = new HashMap<>();
        var sub = platform.portfolio();
        switch (sub) {
            case PortfolioResponse.Portfolio x -> {
                for (var iter: x.portfolio()) {
                    listOfInstruments.put(iter.instrument().symbol(), iter.qty());
                }
            }
            case PortfolioResponse.Other y -> {
                logger.info("{}", y.restResponse());
            }
        }
        return listOfInstruments;
    }

    public Long calculateSMA(List<Long> input) {
        Long sum = 0L;
        if (input.isEmpty()) {
            return 0L;
        }
        for (Long num: input) {
            sum += num;
        }
        return sum/input.size();
    }

    public Long calculateEMA(List<Long> input) {
        if (input.isEmpty()) {
            return 0L;
        }

        int period = input.size();

        double multiplier = 2.0 / (period + 1);
        double ema = input.get(0);

        for (int i = 1; i < input.size(); i++) {
            double currentPrice = input.get(i);
            ema = (currentPrice - ema) * multiplier + ema;
        }

        return Math.round(ema);
    }

    public void strategy(Instrument ins) { //EMA AND SMA
        var respon = platform.history(new HistoryRequest(new Instrument(ins.symbol())));
        switch (respon) {
            case HistoryResponse.History x -> {
                // historical data
//                logger.info("{}", x.bought());
//                logger.info("{}", x.sold());
                Instant currentTime = Instant.now();
                Instant startTime = currentTime.minus(Duration.ofMinutes(20));

                List<Long> bought_prices = new ArrayList<Long>();
                List<Long> sold_prices = new ArrayList<Long>();

                for (ProcessedOrder.Bought rec: x.bought()) {
                    if (rec.created().isAfter(startTime) && rec.created().isBefore(currentTime)) {
                        bought_prices.add(rec.offer().price());
                    }
                }
                for (ProcessedOrder.Sold rec: x.sold()) {
                    if (rec.created().isAfter(startTime) && rec.created().isBefore(currentTime)) {
                        sold_prices.add(rec.offer().price());
                    }
                }

                //streamy, zsumowanie wartosci, mozna dodawac warunki orElse, wiele innych przydatnych metod
//                final DoubleStream sumStream = x
//                        .bought()
//                        .stream()
//                        .mapToLong(bought -> bought.offer().price()).average().stream().map();

                logger.info("sold_prices ({}), {}", ins.symbol(), sold_prices);
                logger.info("bought_prices ({}), {}", ins.symbol(), bought_prices);

                if (!sold_prices.isEmpty() && !bought_prices.isEmpty() && sold_prices.size() > 20) {
                    Long min_buy = Collections.min(sold_prices);
                    Long SMA = calculateSMA(sold_prices);
                    Long EMA = calculateEMA(sold_prices);
                    logger.info("SMA, EMA: {} {}", SMA, EMA);
                    if (EMA > SMA) { //kupowac
                        logger.info("BUY: {}", ins.symbol());
//                        var s = platform.submit(
//                                        new SubmitOrderRequest.Buy(ins.symbol(),
//                                        UUID.randomUUID().toString(),
//                                    20,
//                                        min_buy));
//                        switch (s) {
//                            case SubmitOrderResponse.Acknowledged ack -> {
//                                logger.info("{}", ack);
//                            }
//                            case SubmitOrderResponse.Rejected rej -> {
//                                logger.info("{}", rej.becauseOf());
//                            }
//                            case SubmitOrderResponse.Other oth -> {
//                                logger.info("{}", oth.restResponse());
//                            }
//                        }
                    }
                    else if (EMA < SMA) { //sprzedawac
                        logger.info("SELL {}", ins.symbol());
                    }
                }
            }
            case HistoryResponse.Other error -> {
                logger.info("{}", error.restResponse());
            }
        }
    }

    @Override
    public void run() {
//        SubmittedResponse.Submitted s = Test();
        long avaliableCash = getCash();
//        Map<String, Long> avaliableInstruments = getPortfolioInstruments();
        logger.info("{}", getCash());
        var p = platform.portfolio(); //obiekt portfolio
//        logger.info("{}", getPortfolio());

        switch (p) {
            case PortfolioResponse.Portfolio port -> {
                for (Portfolio.Element elem: port.portfolio())  {
                    strategy(elem.instrument());
                }
            }
            case PortfolioResponse.Other xd -> {
                logger.info("{}", xd.restResponse());
            }
        }


//        strategy(new Instrument("YOLO"));
        logger.info("RUNNING");



//        buyInstrument(p, new Instrument("4FUNMEDIA"), 1, 100);
//        for (var iter: p.portfolio()) {
//            logger.info("{}", iter.instrument());
//            sellInstrument(p, iter.instrument(), 1, 105);
//        }
    }
}
