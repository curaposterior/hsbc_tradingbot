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

    public InstrumentsResponse.Instruments getInstruments() {
        var req = platform.instruments();
        switch (req) {
            case InstrumentsResponse.Instruments x -> {
                return x;
            }
            case InstrumentsResponse.Other y -> {
                logger.info("{}", y);
            }
        }
        return null;
    }

    public int countToBuy() {
        PortfolioResponse.Portfolio p = getPortfolio();
        return p.toBuy().size();
    }

    public int countToSell() {
        PortfolioResponse.Portfolio p = getPortfolio();
        return p.toSell().size();
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

    public void sellInstrment(Instrument i, Long qty, Long price) {
        var s = platform.submit(new SubmitOrderRequest.Sell(
                i.symbol(), UUID.randomUUID().toString(), qty, price)
        );
        switch (s) {
            case SubmitOrderResponse.Acknowledged ack -> {
                logger.info("{}", ack);
            }
            case SubmitOrderResponse.Rejected rej -> {
                logger.info("{}", rej.becauseOf());
            }
            case SubmitOrderResponse.Other oth -> {
                logger.info("{}", oth.restResponse());
            }
        }
    }

    public void buyInstrument(Instrument i, Long qty, Long price) {
        var s = platform.submit(new SubmitOrderRequest.Buy(
                i.symbol(), UUID.randomUUID().toString(), qty, price)
        );
        switch (s) {
            case SubmitOrderResponse.Acknowledged ack -> {
                logger.info("{}", ack);
            }
            case SubmitOrderResponse.Rejected rej -> {
                logger.info("{}", rej.becauseOf());
            }
            case SubmitOrderResponse.Other oth -> {
                logger.info("{}", oth.restResponse());
            }
        }
    }

    public double calculateSMA(List<Long> input) {
        double sum = 0;
        if (input.isEmpty()) {
            return 0L;
        }
        for (Long num: input) {
            sum += num;
        }
        return sum/input.size();
    }

    public double calculateEMA(List<Long> input) {
        if (input.isEmpty()) {
            return 0;
        }

        int period = input.size();

        double multiplier = 2.0 / (period + 1);
        double ema = input.get(0);

        for (int i = 1; i < input.size(); i++) {
            double currentPrice = input.get(i);
            ema = (currentPrice - ema) * multiplier + ema;
        }

//        return Math.round(ema);
        return ema;
    }

    public void strategy(Instrument ins) { //EMA AND SMA
        var respon = platform.history(new HistoryRequest(new Instrument(ins.symbol())));
        switch (respon) {
            case HistoryResponse.History x -> {
                // historical data
                Instant currentTime = Instant.now();
                Instant startTime = currentTime.minus(Duration.ofMinutes(40));

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


                logger.info("sold_prices ({}), {}", ins.symbol(), sold_prices);
//                logger.info("bought_prices ({}), {}", ins.symbol(), bought_prices);

                if (!sold_prices.isEmpty() && !bought_prices.isEmpty() && bought_prices.size() > 20) {
                    Long min_buy = Collections.min(bought_prices);
                    double SMA = calculateSMA(sold_prices);
                    double EMA = calculateEMA(sold_prices);
                    logger.info("{}, SMA, EMA: {} {}", ins.symbol(), SMA, EMA);
                    if (EMA > SMA && getCash() > 80000 && countToBuy() <= 20) { //kupowac
                        logger.info("BUY: {}", ins.symbol());
//                        buyInstrument(ins, 5L, (long) (SMA*0.9));
                        buyInstrument(ins, 5L, (long) (SMA));
                    }
                    else if (EMA < SMA && countToSell() <= 60) { //sprzedawac
                        logger.info("SELL: {}", ins.symbol());
//                        sellInstrment(ins, 8L, Integer.toUnsignedLong(Math.toIntExact((long) (SMA*1.1))));
                        sellInstrment(ins, 8L, Integer.toUnsignedLong(Math.toIntExact((long) (min_buy))));
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
//        InstrumentsResponse.Instruments ins = getInstruments();
//        for (var item: ins.available()) {
//            strategy(new Instrument(item.symbol()));
//        }


//        strategy(new Instrument("YOLO"));
        logger.info("RUNNING");



//        buyInstrument(p, new Instrument("4FUNMEDIA"), 1, 100);
//        for (var iter: p.portfolio()) {
//            logger.info("{}", iter.instrument());
//            sellInstrument(p, iter.instrument(), 1, 105);
//        }
    }
}
