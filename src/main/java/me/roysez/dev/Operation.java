package me.roysez.dev;

public enum Operation {


    GET_STARTED,
    DOCUMENT_TRACKING ;

    public static Operation getOperation(Integer i) throws IllegalArgumentException {
        switch (i) {
            case 1:
                return GET_STARTED;
            case 2:
                return DOCUMENT_TRACKING;
            default:
                throw new IllegalArgumentException();
        }

    }

}
