from omnipytent import *
from omnipytent.execution import ShellCommandExecuter
from omnipytent.completers import file_completer
from omnipytent.ext.idan import local, ERUN, gradle


if FN.exists('g:ale_java_javac_classpath') and not VAR['g:ale_java_javac_classpath']:
    VAR['g:ale_java_javac_classpath'] = FN['javacomplete#server#GetClassPath']()


run_py = local['./run.py']


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
    gradle['build']['-x', 'test'] & ERUN.bang


@task
def run(ctx):
    pass
    # cmd = run_py['experiment', 'info']
    # cmd = run_py['experiment', 'run']['transmittionRadius=10', 'markerRadius=20']
    # cmd = run_py['experiment', 'run']['faces=20']
    # cmd = run_py['dp', 'add']['transmittionRadius=10', 'markerRadius=20']
    # cmd = run_py['dp', 'add']['-n', 21, 'faces=5', 'num=10']
    # cmd = run_py['dp', 'list']
    # cmd = run_py['run']
    # cmd = run_py['run', '--help']

    cmd & BANG


@task
def act(ctx):
    cmd = run_py['--format=csv']['dp', 'list'] | local['column']['-ts,']
    cmd = run_py['--format=csv']['er', 'list'] | local['head'] | local['column']['-ts,']

    cmd & BANG


@task
def clean(ctx):
    gradle['clean'] & BANG


@task.options
def the_test_to_run(ctx):
    core_BasicExperimentRunningTest = 'nisui.core.BasicExperimentRunningTest'
    core_QueryParserTest = 'nisui.core.QueryParserTest'
    h2_store_BuildTablesTest = 'nisui.h2_store.BuildTablesTest'
    h2_store_FillAndReadDataTest = 'nisui.h2_store.FillAndReadDataTest'
    core_DynamicValuesTest = 'nisui.core.DynamicValuesTest'
    java_runner_JavaValuesTest = 'nisui.java_runner.JavaValuesTest'
    java_runner_JavaExperimentTest = 'nisui.java_runner.JavaExperimentTest'
    cli_ExperimentCommandsTests = 'nisui.cli.ExperimentCommandsTests'
    cli_DataPointCommandsTests = 'nisui.cli.DataPointCommandsTests'
    cli_ExperimentResultCommandsTests = 'nisui.cli.ExperimentResultCommandsTests'


@task(the_test_to_run)
def test(ctx, *args):
    # gradle_tests() & BANG
    gradle_tests(ctx.dep.the_test_to_run)[args] & ERUN.bang
test.complete(lambda ctx: {'--info', '--debug', '--scan'})


@task
def test_all(ctx):
    local['gradle']['test'] & BANG


@task
def edit_runner_file(ctx):
    CMD.edit('/home/idanarye/links/study/Thesis/VocalTerritorySimulator/RunWithNisui.java')


def lombok_jar_file():
    candidates = (local.path(p) for p in local['find']['/home/idanarye/.gradle', '-name', 'lombok-*.jar']().splitlines())
    return max(candidates, key=lambda c: c.basename)


@task
def jar(ctx):
    local['gradle']['fatJar'] & BANG


_vts_args = [
    '/home/idanarye/links/study/Thesis/VocalTerritorySimulator/RunWithNisui.java',
    '/home/idanarye/links/study/Thesis/VocalTerritorySimulator/build/VocalTerritorySimulator.jar',
]
_vts_data_points = '/home/idanarye/links/study/Thesis/VocalTerritorySimulator/data-points.txt'

@task
def test_with_vts(ctx):
    cmd = gradle['run']['-Pargs=%s' % ' '.join(_vts_args)]
    cmd = cmd < _vts_data_points
    cmd & TERMINAL_PANEL
