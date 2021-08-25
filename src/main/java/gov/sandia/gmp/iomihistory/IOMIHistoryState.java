package gov.sandia.gmp.iomihistory;

import java.io.Serializable;

/**
 * The list of History Operations defined for all IOMIObjects.
 * <p>
 * TIME ................ A full time update (long). All other input times
 * between consecutive TIME entries are given as a
 * byte offset from the last input TIME entry.
 * Entry is 1 byte for the operation and 8 bytes
 * for the time (long).
 * <p>
 * OUTPUTHISTORY ....... Occurs when a history file is output. Entry is 1
 * byte for the operation and 1 byte for the time
 * offset.
 * <p>
 * VMMEMORYCHECK ....... The amount of memory used by the virtual machine.
 * (set with ioIndx = -1). Includes an amount (long).
 * Entry is 1 byte for the operation, 1 byte for the
 * time offset, and 8 bytes for the amount of memory
 * used.
 * GCSTART ............. The start of a virtual machine manual GC call.
 * (set with ioIndx = -1). Entry is 1 byte for the
 * operation, 1 byte for the time offset.
 * GCEND ............... The the end of a virtual machine GC Call.
 * (set with ioIndx = -1). Includes an amount (long).
 * Entry is 1 byte for the operation, 1 byte for the
 * time offset, and 8 bytes for the amount of memory
 * used.
 * <p>
 * CREATE .............. An IOMI object is instantiated by the host. Entry
 * is 1 byte for operation, 4 bytes for index, and 1
 * byte for time offset.
 * DESTROY ............. An IOMI object is destroyed by the host. Entry
 * is 1 byte for operation, 4 bytes for index, and 1
 * byte for time offset.
 * RELOAD .............. An IOMI object is reloaded following a previous
 * destruction. Entry is 1 byte for operation, 4
 * bytes for index, and 1 byte for time offset.
 * QUEUEIO ............. An IOMI object has requested an IO operation but
 * has been put into a wait queue until others finish.
 * Entry is 1 byte for operation, 4 bytes for index,
 * and 1 byte for time offset.
 * REMOVEQUEUEIO ....... An IOMI object has been removed from the IO queue.
 * Followed by an immediate allow. Entry is 1 byte
 * for operation, 4 bytes for index, and 1 byte for
 * time offset.
 * ALLOWIO ............. An IOMI object has requested an IO operation and
 * has been granted approval to proceed. Entry is 1
 * byte for operation, 4 bytes for index, and 1 byte
 * for time offset.
 * COMPLETEIOREAD ...... An IOMI object has completed an IO read operation.
 * Entry is 1 byte for operation, 4 bytes for index,
 * and 1 byte for time offset.
 * COMPLETEIOWRITE ..... An IOMI object has completed an IO write operation.
 * Entry is 1 byte for operation, 4 bytes for index,
 * and 1 byte for time offset.
 * COMPLETEIORENAME .... An IOMI object has completed an IO rename
 * operation. Entry is 1 byte for operation, 4 bytes
 * for index, and 1 byte for time offset.
 * COMPLETEIODELETE .... An IOMI object has completed an IO delete
 * operation. Entry is 1 byte for operation, 4 bytes
 * for index, and 1 byte for time offset.
 * LOADDATA ............ An IOMI object has loaded its data (read, fill,
 * or assignment). Entry is 1 byte for operation, 4
 * bytes for index, and 1 byte for time offset.
 * ADDREFERENCE ........ An IOMI object has been referenced by a thread.
 * Entry is 1 byte for operation, 4 bytes for index,
 * and 1 byte for time offset.
 * REMOVEREFERENCE ..... An IOMI object has been dereferenced by a thread.
 * Entry is 1 byte for operation, 4 bytes for index,
 * and 1 byte for time offset.
 * ADDCACHE ............ An IOMI object is no longer thread referenced and
 * is added to the cache for potential use in the
 * future. Entry is 1 byte for operation, 4 bytes for
 * index, and 1 byte for time offset.
 * REMOVECACHEREREF .... An IOMI object is removed from cache because it
 * has been re-referenced by another thread. Entry is
 * 1 byte for operation, 4 bytes for index, and 1
 * byte for time offset.
 * REMOVECACHECNTLMT ... An IOMI object is removed from cache because the
 * cache count limit has been exceeded (awaiting
 * destruction). Entry is 1 byte for operation, 4
 * bytes for index, and 1 byte for time offset.
 * REMOVECACHEMEMLMT ... An IOMI object is removed from cache because of
 * excessive memory use (awaiting destruction).
 * Entry is 1 byte for operation, 4 bytes for index,
 * and 1 byte for time offset.
 * THREADDELAY ......... An IOMI object has suffered a thread delay
 * (waiting for another thread to complete a read
 * operation). Includes a duration (long). Entry is
 * 1 byte for operation, 4 bytes for index, 1 byte
 * for time offset, and 8 bytes for the duration
 * amount.
 *
 * @author jrhipp
 */
public enum IOMIHistoryState implements Serializable, HistoryOperation {
    TIME {
        @Override
        public boolean hasIndex() {
            return false;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 9;
        } // op(1), Ltime(8)

        @Override
        public boolean isTime() {
            return true;
        }
    },

    OUTPUTHISTORY {
        @Override
        public boolean hasIndex() {
            return false;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 2;
        } // op(1), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },

    VMMEMORYCHECK {
        @Override
        public boolean hasIndex() {
            return false;
        }

        @Override
        public boolean hasExtraLong() {
            return true;
        }

        @Override
        public int operationLength() {
            return 10;
        } // op(1), Btime(1), extra(8)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    GCSTART {
        @Override
        public boolean hasIndex() {
            return false;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 2;
        } // op(1), Btime(1), extra(8)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    GCEND {
        @Override
        public boolean hasIndex() {
            return false;
        }

        @Override
        public boolean hasExtraLong() {
            return true;
        }

        @Override
        public int operationLength() {
            return 10;
        } // op(1), Btime(1), extra(8)

        @Override
        public boolean isTime() {
            return false;
        }
    },

    CREATE {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    DESTROY {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    RELOAD {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    QUEUEIO {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    REMOVEQUEUEIO {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    ALLOWIO {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    COMPLETEIOREAD {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    COMPLETEIOWRITE {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    COMPLETEIORENAME {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    COMPLETEIODELETE {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    LOADDATA {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    ADDREFERENCE {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    REMOVEREFERENCE {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    ADDCACHE {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    REMOVECACHEREREF {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    REMOVECACHECNTLMT {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    REMOVECACHEMEMLMT {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return false;
        }

        @Override
        public int operationLength() {
            return 6;
        } // op(1), indx(4), Btime(1)

        @Override
        public boolean isTime() {
            return false;
        }
    },
    THREADDELAY {
        @Override
        public boolean hasIndex() {
            return true;
        }

        @Override
        public boolean hasExtraLong() {
            return true;
        }

        @Override
        public int operationLength() {
            return 14;
        } // op(1), indx(4), Btime(1), extra(8)

        @Override
        public boolean isTime() {
            return false;
        }
    };

    /**
     * Return time ordinal for this history state.
     */
    public static int TimeOrdinal = IOMIHistoryState.TIME.ordinal();

    /**
     * Return output ordinal for this history state.
     */
    public static int OutputOrdinal = IOMIHistoryState.OUTPUTHISTORY.ordinal();
}
