package com.sucy.skill.dynamic.data;

public class DataTagData {

    public String key;

    private Double value;

    public Long overTime;

    public Integer time;

    //这里传入的是tick 自动转换为时间戳
    public DataTagData(String key, Double value, Integer time) {
        this.key = key;
        this.value = value;
        this.overTime = System.currentTimeMillis() + (time * 20000);
        this.time = time;
    }

    public double getValue() {
        if (overTime < System.currentTimeMillis() && time != -1) {
            return 0;
        }
        return value;
    }

    public void setValue(Double target, String action) {
        switch (action) {
            case "+": {
                this.value = getValue() + target;
                break;
            }
            case "-": {
                this.value = getValue() - target;
                break;
            }
            case "*": {
                this.value = getValue() * target;
                break;
            }
            case "/": {
                this.value = getValue() / target;
                break;
            }
        }
        this.overTime = System.currentTimeMillis() + (time * 20000);
    }

}
