package gov.sandia.gmp.iomi;

import java.io.Serializable;

/**
 * File operations supported by IOMI. Database operations (or others) could
 * also be added.
 *
 * @author jrhipp
 */
public enum IOMIFileOp implements Serializable {
    READ,
    WRITE,
    RENAME,
    DELETE;
}
