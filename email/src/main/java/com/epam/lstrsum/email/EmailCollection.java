package com.epam.lstrsum.email;

import javax.mail.Address;

/**
 * Implement this interface to define new way of obtaining emails set.
 */

public interface EmailCollection<T> {

    /**
     * Get set of email for given parameter.
     *
     * @param param - parameter
     * @return - set of emails or empty set
     */
    Address[] getEmailAddresses(T param);
}
