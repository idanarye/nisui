from omnipytent import *
from omnipytent.integration.plumbum import local
from omnipytent.execution import ShellCommandExecuter


if not VAR['g:ale_java_javac_classpath']:
    VAR['g:ale_java_javac_classpath'] = FN['javacomplete#server#GetClassPath']()


@ShellCommandExecuter
def ERUN(command):
    CMD.Erun.bang(command)

gradle = local['gradle']['-q']


def subproject_names():
    for path in local.path('.'):
        if (path / 'src').exists():
            yield path.basename


def gradle_tests(test):
    for subproject_name in subproject_names():
        if test.startswith('nisui.%s.' % subproject_name):
            cmd = local['gradle'][':%s:test' % subproject_name]
            break
    else:
        cmd = local['gradle']['test']
    cmd = cmd['--tests', test]
    return cmd


@task
def compile(ctx):
    gradle['build']['-x', 'test'] & ERUN


@task
def run(ctx):
    gradle['run']['-Pargs=/home/idanarye/links/study/Thesis/VocalTerritorySimulator/RunWithNisui.java /home/idanarye/links/study/Thesis/VocalTerritorySimulator/build/VocalTerritorySimulator.jar'] & TERMINAL_PANEL


@task
def clean(ctx):
    gradle['clean'] & BANG


@task
def test(ctx):
    # gradle_tests() & BANG
    gradle_tests('nisui.core.BasicExperimentRunningTest') & ERUN

