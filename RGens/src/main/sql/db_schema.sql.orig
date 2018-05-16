create table Rules (
	ruleid serial,
	name varchar(255) not null,
	casecount int,
	primary key(id)
);

create index Rules_name on Rules(name);

create table RuleCases (
	ruleid serial,
	cse varchar(255) array
);

create index RuleCase_id on RuleCases(ruleid);
