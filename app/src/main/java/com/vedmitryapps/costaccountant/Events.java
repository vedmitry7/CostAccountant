package com.vedmitryapps.costaccountant;

public class Events {
    public static class ClickProduct {
        private final int position;

        public ClickProduct(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }

    public static class ClickDay {
        private final String dayId;

        public ClickDay(String position) {
            this.dayId = position;
        }

        public String getDayId() {
            return dayId;
        }
    }


}
