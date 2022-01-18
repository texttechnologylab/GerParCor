import logging
import multiprocessing as mp
import time

logger = mp.log_to_stderr(logging.WARNING)

def worker(x):
    try:
        if not terminating.is_set():
            logger.warn("Running worker({x!r})".format(x = x))
            time.sleep(3)
        else:
            logger.warn("got the message... we're terminating!")
    except KeyboardInterrupt:
        logger.warn("terminating is set")
        terminating.set()
    return x

def initializer(terminating_):
    # This places terminating in the global namespace of the worker subprocesses.
    # This allows the worker function to access `terminating` even though it is
    # not passed as an argument to the function.
    global terminating
    terminating = terminating_

def main():
    terminating = mp.Event()
    result = []
    pool = mp.Pool(initializer=initializer, initargs=(terminating, ))
    params = range(12)
    try:
         logger.warn("starting pool runs")
         result = pool.map(worker, params)
         pool.close()
    except KeyboardInterrupt:
        logger.warn("^C pressed")
        pool.terminate()
    finally:
        pool.join()
        logger.warn('done: {r}'.format(r = result))

if __name__ == '__main__':
    main()