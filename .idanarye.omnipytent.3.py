from omnipytent import *
from omnipytent.integration.plumbum import local
from omnipytent.execution import ShellCommandExecuter


if FN.exists('g:ale_java_javac_classpath') and not VAR['g:ale_java_javac_classpath']:
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
    local['tmpplot/run.py'] & BANG
    return
    cmd = gradle['run']['-Pargs=%s' % ' '.join([
        '/home/idanarye/links/study/Thesis/VocalTerritorySimulator/RunWithNisui.java',
        '/home/idanarye/links/study/Thesis/VocalTerritorySimulator/build/VocalTerritorySimulator.jar',
    ])]
    cmd = cmd < '/home/idanarye/links/study/Thesis/VocalTerritorySimulator/data-points.txt'
    cmd = cmd | local['tee']['-a', 'results.txt']
    with local.path('runit.sh').open('w') as f:
        f.write(str(cmd))
        f.write('\n')
    cmd & TERMINAL_PANEL


@task
def clean(ctx):
    gradle['clean'] & BANG


@task
def test(ctx):
    # gradle_tests() & BANG
    gradle_tests('nisui.core.BasicExperimentRunningTest') & ERUN


@task
def edit_runner_file(ctx):
    CMD.edit('/home/idanarye/links/study/Thesis/VocalTerritorySimulator/RunWithNisui.java')


def lombok_jar_file():
    candidates = (local.path(p) for p in local['find']['/home/idanarye/.gradle', '-name', 'lombok-*.jar']().splitlines())
    return max(candidates, key=lambda c: c.basename)


@task
def jar(ctx):
    local['gradle']['fatJar'] & BANG
