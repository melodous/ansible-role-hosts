from ansible import errors
import re

def add_secrets_to_cluster(clusters, secrets):
    for cluster in clusters:
        secret = filter_list(secrets,'cluster',cluster['cluster'])[0]
        for k,v in secret.iteritems():
            cluster['environment'][k] = v
    return clusters

def filter_list(list, key, value):
    try:
        return filter(lambda t: t[key] == value, list)
    except KeyError, e:
        raise errors.AnsibleFilterError('filter_list plugin error: %s, string=%s' % str(e),str(string) )

def append(list, value):
    return [ item + value for item in list ]

def prepend(list, value):
    return [ value + item for item in list ]

def merge( hash_a, hash_b ):
    return dict(hash_a.items() + hash_b.items());

def split(string, separator=' '):
    try:
        return string.strip().split(separator)
    except Exception, e:
        raise errors.AnsibleFilterError('split plugin error: %s, string=%s' % str(e),str(string) )

def filter_if_defined(string, env_filter):
    try:
        if env_filter == "empty":
            return True
        elif string in env_filter.split(","):
            return True
        else:
            return False
    except Exception, e:
        raise errors.AnsibleFilterError('filter_if_defined plugin error: %s, string=%s' % (str(e),str(string)) )
        #raise errors.AnsibleFilterError('filter_if_defined plugin error: %s, string=%s' % str(e),str(string) )


def islist(object):
    return isinstance(object, list)

def split_regex(string, seperator_pattern):
    try:
        return re.split(seperator_pattern, string)
    except Exception, e:
        raise errors.AnsibleFilterError('split plugin error: %s, string=%s' % str(e),str(string))

class FilterModule(object):
    def filters(self):
        return {
            'byattr': filter_list,
            'append': append,
            'prepend': prepend,
            'split': split,
            'merge' : merge,
            'islist': islist,
            'split_regex' : split_regex,
            'add_secrets_to_cluster' : add_secrets_to_cluster,
            'filter_if_defined': filter_if_defined,
        }
