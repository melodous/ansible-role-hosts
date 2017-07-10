def testEntries(Command):
	#Command.run_test("echo '127.0.54.45    thisisatest' >> /etc/hosts")
	Command.run_test("getent ahosts thisisatest")
