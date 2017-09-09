package me.roysez.dev;

public enum Operation {


    GET_STARTED,
    DOCUMENT_TRACKING,
    GET_WAREHOUSES;

    public static Operation getOperation(Integer i) throws IllegalArgumentException {
        switch (i) {
            case 1:
                return GET_STARTED;
            case 2:
                return DOCUMENT_TRACKING;
            case 3:
                return GET_WAREHOUSES;
            default:
                throw new IllegalArgumentException();
        }

    }

}
