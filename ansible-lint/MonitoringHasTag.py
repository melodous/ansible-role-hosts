import ansiblelint.utils
from ansiblelint import AnsibleLintRule

class MonitoringHasTag(AnsibleLintRule):
    id = 'INSTEL0001'
    shortdesc = 'Monitoring Tasks must have tag'
    description = 'All task related with monitoring and cyclops should have tag monitoring'
    tags = ['monitoring']


    def matchtask(self, file, task):
        # If the task include another task or make the playbook fail
        # Don't force to have a tag
        if not set(task.keys()).isdisjoint(['include','fail']):
            return False
        #Only check monitoring playbooks
        if "monitoring.yml" not in str(file['path']):
            return False
        # Task should have tag 'monitoring'
        if task.has_key('tags') and 'monitoring' in task['tags']:
            return False
        return True
