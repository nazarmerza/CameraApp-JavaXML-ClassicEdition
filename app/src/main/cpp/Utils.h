/*
 * Utils.h
 *
 *  Created on: Mar 24, 2014
 *      Author: nazar
 */

#ifndef UTILS_H_
#define UTILS_H_


#include <sys/time.h>

namespace Utils {
long long currentTimeInMilliseconds() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return ((tv.tv_sec * 1000) + (tv.tv_usec / 1000));
}

}
#endif /* UTILS_H_ */
